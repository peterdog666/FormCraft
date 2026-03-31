<template>
  <div style="padding: 20px;">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
      <h2>表单管理</h2>
      <el-button type="primary" @click="handleCreate">创建表单</el-button>
    </div>

    <el-table :data="formList" border style="width: 100%;" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="表单名称" min-width="180" />
      <el-table-column prop="description" label="描述" min-width="180" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '已发布' : '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="submissionCount" label="提交数" width="100" />
      <el-table-column label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-button type="success" link @click="handleFill(row)" :disabled="row.status !== 1">填写</el-button>
          <el-button type="warning" link @click="handleData(row)">数据</el-button>
          <el-button type="primary" link @click="handlePublish(row)" v-if="row.status !== 1">发布</el-button>
          <el-button type="info" link @click="handleUnpublish(row)" v-else>停用</el-button>
          <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formApi } from '../api/index.js'

const router = useRouter()
const formList = ref([])
const loading = ref(false)

const loadForms = async () => {
  loading.value = true
  try {
    const res = await formApi.list()
    formList.value = res.data || []
  } catch (error) {
    console.error('加载表单列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  router.push('/design')
}

const handleEdit = (row) => {
  router.push(`/design/${row.id}`)
}

const handleFill = (row) => {
  window.open(`#/fill/${row.id}`, '_blank')
}

const handleData = (row) => {
  router.push(`/data/${row.id}`)
}

const handlePublish = async (row) => {
  try {
    await formApi.publish(row.id)
    ElMessage.success('发布成功')
    loadForms()
  } catch (error) {
    console.error('发布失败:', error)
  }
}

const handleUnpublish = async (row) => {
  try {
    await formApi.unpublish(row.id)
    ElMessage.success('已停用')
    loadForms()
  } catch (error) {
    console.error('停用失败:', error)
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该表单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await formApi.delete(row.id)
    ElMessage.success('删除成功')
    loadForms()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  loadForms()
})
</script>
