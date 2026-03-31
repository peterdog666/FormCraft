<template>
  <div style="padding: 20px;">
    <div style="margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center;">
      <div>
        <h2>{{ formName }} - 提交记录</h2>
      </div>
      <div>
        <el-button @click="handleBack">返回</el-button>
        <el-button type="success" @click="handleExport">导出数据</el-button>
      </div>
    </div>

    <el-table :data="submissions" border style="width: 100%;" v-loading="loading">
      <el-table-column label="序号" width="70" type="index" />
      <el-table-column label="提交时间" width="180">
        <template #default="{ row }">
          {{ formatTime(row.submitTime) }}
        </template>
      </el-table-column>
      <el-table-column label="IP地址" width="150">
        <template #default="{ row }">
          {{ row.ip || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="提交数据" min-width="300">
        <template #default="{ row }">
          <div v-if="row.formSchema">
            <div v-for="field in row.formSchema" :key="field.id" style="margin-bottom: 8px;">
              <strong>{{ field.label }}:</strong>
              <span v-if="Array.isArray(row.data[field.id])">{{ row.data[field.id].join(', ') }}</span>
              <span v-else>{{ row.data[field.id] || '-' }}</span>
            </div>
          </div>
          <div v-else style="color: #999; font-size: 12px;">
            {{ row.data }}
          </div>
        </template>
      </el-table-column>
    </el-table>

    <div style="margin-top: 20px; text-align: center;">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { formApi, submissionApi } from '../api/index.js'

const props = defineProps(['id'])
const router = useRouter()

const formName = ref('')
const submissions = ref([])
const formSchema = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

const loadFormInfo = async (formId) => {
  try {
    const res = await formApi.getById(formId)
    if (res.data) {
      formName.value = res.data.name
      formSchema.value = JSON.parse(res.data.schema || '[]')
    }
  } catch (error) {
    console.error('加载表单信息失败:', error)
  }
}

const loadSubmissions = async () => {
  loading.value = true
  try {
    const formId = props.id || router.currentRoute.value.params.id
    const res = await submissionApi.getList(formId, currentPage.value, pageSize.value)
    
    if (res.data && res.data.records) {
      total.value = res.data.total || 0
      submissions.value = res.data.records.map(record => ({
        ...record,
        data: parseJson(record.data),
        formSchema: formSchema.value
      }))
    } else {
      total.value = 0
      submissions.value = []
    }
  } catch (error) {
    console.error('加载提交记录失败:', error)
  } finally {
    loading.value = false
  }
}

const parseJson = (str) => {
  try {
    return JSON.parse(str || '{}')
  } catch {
    return {}
  }
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadSubmissions()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadSubmissions()
}

const handleBack = () => {
  router.push('/')
}

const handleExport = () => {
  if (submissions.value.length === 0) {
    ElMessage.warning('暂无数据可导出')
    return
  }

  const headers = ['序号', '提交时间', 'IP地址', ...formSchema.value.map(f => f.label)]
  const rows = submissions.value.map((sub, index) => {
    const row = [
      index + 1,
      formatTime(sub.submitTime),
      sub.ip || '-'
    ]
    formSchema.value.forEach(field => {
      const val = sub.data[field.id]
      if (Array.isArray(val)) {
        row.push(val.join('; '))
      } else {
        row.push(val || '-')
      }
    })
    return row
  })

  const csvContent = [
    headers.join(','),
    ...rows.map(row => row.map(cell => `"${(cell || '').toString().replace(/"/g, '""')}"`).join(','))
  ].join('\n')

  const BOM = '\uFEFF'
  const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `${formName.value}_提交记录_${new Date().toISOString().slice(0, 10)}.csv`
  link.click()
  URL.revokeObjectURL(link.href)

  ElMessage.success('导出成功')
}

onMounted(() => {
  const formId = props.id || router.currentRoute.value.params.id
  loadFormInfo(formId)
  loadSubmissions()
})
</script>
