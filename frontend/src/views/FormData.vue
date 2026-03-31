<template>
  <div style="padding: 20px;">
    <!-- 顶部工具栏 -->
    <div style="margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px;">
      <div>
        <h2 style="margin: 0;">{{ formName }} - 提交记录</h2>
      </div>
      <div style="display: flex; align-items: center; gap: 10px; flex-wrap: wrap;">
        <!-- 日期范围筛选 -->
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width: 260px;"
          @change="handleDateRangeChange"
        />
        <!-- 导出下拉菜单 -->
        <el-dropdown @command="handleExportCommand" trigger="click">
          <el-button type="success">
            导出数据
            <el-icon style="margin-left: 4px;"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="csv">
                <el-icon><Document /></el-icon>
                导出 CSV
              </el-dropdown-item>
              <el-dropdown-item command="excel">
                <el-icon><Grid /></el-icon>
                导出 Excel
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button @click="handleBack">返回</el-button>
      </div>
    </div>

    <!-- 数据表格 -->
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

    <!-- 分页 -->
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
import { ElMessage, ElLoading } from 'element-plus'
import { ArrowDown, Document, Grid } from '@element-plus/icons-vue'
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

// 日期范围：[startDate, endDate]，格式 YYYY-MM-DD
const dateRange = ref(null)

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

const handleDateRangeChange = () => {
  // 日期范围变化时重新加载（可选：也可以只在导出时使用）
  currentPage.value = 1
  loadSubmissions()
}

const handleBack = () => {
  router.push('/')
}

/** 导出命令分发 */
const handleExportCommand = (command) => {
  if (command === 'csv') {
    exportCsv()
  } else if (command === 'excel') {
    exportExcel()
  }
}

/** 导出 CSV（前端生成） */
const exportCsv = () => {
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
    ...rows.map(row =>
      row.map(cell => `"${(cell || '').toString().replace(/"/g, '""')}"`).join(',')
    )
  ].join('\n')

  const BOM = '\uFEFF'
  const blob = new Blob([BOM + csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `${formName.value}_提交记录_${new Date().toISOString().slice(0, 10)}.csv`
  link.click()
  URL.revokeObjectURL(link.href)
  ElMessage.success('CSV 导出成功')
}

/** 导出 Excel（调用后端接口） */
const exportExcel = async () => {
  const formId = props.id || router.currentRoute.value.params.id
  const startDate = dateRange.value ? dateRange.value[0] : null
  const endDate   = dateRange.value ? dateRange.value[1] : null

  const loadingInstance = ElLoading.service({
    text: '正在生成 Excel，请稍候...',
    background: 'rgba(0,0,0,0.3)'
  })

  try {
    const response = await submissionApi.exportExcel(formId, startDate, endDate)

    // 从响应头获取文件名
    const disposition = response.headers['content-disposition'] || ''
    let fileName = `${formName.value}_提交记录.xlsx`
    const match = disposition.match(/filename\*=UTF-8''(.+)/)
    if (match) {
      fileName = decodeURIComponent(match[1])
    }

    // 触发浏览器下载
    const blob = new Blob([response.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)

    ElMessage.success('Excel 导出成功')
  } catch (error) {
    console.error('Excel 导出失败:', error)
    ElMessage.error('Excel 导出失败，请重试')
  } finally {
    loadingInstance.close()
  }
}

onMounted(() => {
  const formId = props.id || router.currentRoute.value.params.id
  loadFormInfo(formId)
  loadSubmissions()
})
</script>
