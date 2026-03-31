<template>
  <div style="padding: 20px;">
    <div style="margin-bottom: 20px; display: flex; gap: 10px; align-items: center;">
      <el-input v-model="formName" placeholder="请输入表单名称" style="width: 300px;" />
      <el-button type="primary" @click="handleSave">保存</el-button>
      <el-button @click="handlePreview">预览</el-button>
      <el-button @click="handleBack">返回</el-button>
    </div>

    <div style="display: flex; gap: 20px; height: calc(100vh - 180px);">
      <div style="width: 240px; background: white; border-radius: 8px; padding: 16px; overflow-y: auto;">
        <h4>组件面板</h4>
        <div class="component-item" v-for="comp in components" :key="comp.type" @click="addComponent(comp)">
          <el-icon><component :is="comp.icon" /></el-icon>
          <span>{{ comp.label }}</span>
        </div>
      </div>

      <div style="flex: 1; background: white; border-radius: 8px; padding: 16px; overflow-y: auto;">
        <h4>表单画布</h4>
        <draggable 
          v-model="formFields" 
          item-key="id" 
          handle=".drag-handle"
          ghost-class="ghost"
          @end="onDragEnd"
          style="min-height: 400px;"
        >
          <template #item="{ element, index }">
            <div 
              class="field-item"
              :class="{ selected: selectedField?.id === element.id }"
              @click="selectField(element)"
            >
              <el-icon class="drag-handle" style="cursor: move;"><Rank /></el-icon>
              <div style="flex: 1;">
                <span v-if="element.required" style="color: red;">*</span>
                {{ element.label }}
                <span style="color: #999; font-size: 12px;">({{ getTypeName(element.type) }})</span>
              </div>
              <el-button type="danger" link @click.stop="removeField(index)">删除</el-button>
            </div>
          </template>
        </draggable>
        <div v-if="formFields.length === 0" style="text-align: center; color: #999; padding: 60px;">
          从左侧拖拽或点击组件添加字段
        </div>
      </div>

      <div style="width: 280px; background: white; border-radius: 8px; padding: 16px; overflow-y: auto;">
        <h4>属性配置</h4>
        <div v-if="selectedField" class="property-panel">
          <el-form label-width="80px" size="small">
            <el-form-item label="标签">
              <el-input v-model="selectedField.label" @change="updateField" />
            </el-form-item>
            <el-form-item label="占位符">
              <el-input v-model="selectedField.placeholder" @change="updateField" />
            </el-form-item>
            <el-form-item label="必填">
              <el-switch v-model="selectedField.required" @change="updateField" />
            </el-form-item>
            <el-form-item label="类型">
              <span>{{ getTypeName(selectedField.type) }}</span>
            </el-form-item>
            <el-form-item label="选项" v-if="hasOptions(selectedField.type)">
              <div v-for="(opt, idx) in selectedField.options" :key="idx" style="display: flex; gap: 5px; margin-bottom: 5px;">
                <el-input v-model="selectedField.options[idx]" size="small" style="flex: 1;" @change="updateField" />
                <el-button size="small" @click="removeOption(idx)" :disabled="selectedField.options.length <= 1">-</el-button>
              </div>
              <el-button size="small" @click="addOption">添加选项</el-button>
            </el-form-item>
          </el-form>
        </div>
        <div v-else style="color: #999; text-align: center; padding: 40px;">
          请选择一个字段进行配置
        </div>
      </div>
    </div>

    <el-dialog v-model="previewVisible" title="表单预览" width="600px">
      <FormPreview :schema="formFields" @submit="handlePreviewSubmit" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import draggable from 'vuedraggable'
import { Rank, Edit, Document, List, Tickets, SuccessFilled, Calendar, Grid } from '@element-plus/icons-vue'
import { formApi } from '../api/index.js'
import FormPreview from './FormPreview.vue'

const router = useRouter()
const route = useRoute()
const props = defineProps(['id'])

const formName = ref('')
const formFields = ref([])
const selectedField = ref(null)
const previewVisible = ref(false)
const formId = ref(null)

const components = [
  { type: 'input', label: '单行输入', icon: 'Edit' },
  { type: 'textarea', label: '多行文本', icon: 'Document' },
  { type: 'number', label: '数字输入', icon: 'Grid' },
  { type: 'date', label: '日期选择', icon: 'Calendar' },
  { type: 'select', label: '下拉选择', icon: 'List', options: ['选项1', '选项2'] },
  { type: 'radio', label: '单选', icon: 'SuccessFilled', options: ['选项1', '选项2'] },
  { type: 'checkbox', label: '多选', icon: 'Tickets', options: ['选项1', '选项2'] }
]

const getTypeName = (type) => {
  const comp = components.find(c => c.type === type)
  return comp ? comp.label : type
}

const hasOptions = (type) => ['select', 'radio', 'checkbox'].includes(type)

const addComponent = (comp) => {
  const newField = {
    id: Date.now() + '_' + Math.random().toString(36).substr(2, 9),
    type: comp.type,
    label: comp.label,
    placeholder: '',
    required: false,
    options: comp.options ? [...comp.options] : []
  }
  formFields.value.push(newField)
  selectedField.value = newField
}

const selectField = (field) => {
  selectedField.value = field
}

const updateField = () => {
  const index = formFields.value.findIndex(f => f.id === selectedField.value.id)
  if (index !== -1) {
    formFields.value[index] = { ...selectedField.value }
  }
}

const removeField = (index) => {
  formFields.value.splice(index, 1)
  if (selectedField.value && !formFields.value.find(f => f.id === selectedField.value.id)) {
    selectedField.value = null
  }
}

const addOption = () => {
  if (!selectedField.value.options) {
    selectedField.value.options = []
  }
  selectedField.value.options.push('新选项')
  updateField()
}

const removeOption = (index) => {
  selectedField.value.options.splice(index, 1)
  updateField()
}

const onDragEnd = () => {
  if (selectedField.value) {
    selectedField.value = formFields.value.find(f => f.id === selectedField.value.id)
  }
}

const handleSave = async () => {
  if (!formName.value) {
    ElMessage.warning('请输入表单名称')
    return
  }
  if (formFields.value.length === 0) {
    ElMessage.warning('请添加至少一个字段')
    return
  }
  
  const formData = {
    name: formName.value,
    description: '',
    schema: JSON.stringify(formFields.value)
  }
  
  try {
    if (formId.value) {
      await formApi.update(formId.value, formData)
      ElMessage.success('保存成功')
    } else {
      const res = await formApi.create(formData)
      formId.value = res.data.id
      ElMessage.success('创建成功')
    }
  } catch (error) {
    console.error('保存失败:', error)
  }
}

const handlePreview = () => {
  if (formFields.value.length === 0) {
    ElMessage.warning('请先添加字段')
    return
  }
  previewVisible.value = true
}

const handlePreviewSubmit = (data) => {
  console.log('预览提交:', data)
  ElMessage.success('表单验证通过')
}

const handleBack = () => {
  router.push('/')
}

const loadForm = async (id) => {
  try {
    const res = await formApi.getById(id)
    if (res.data) {
      formName.value = res.data.name
      formFields.value = JSON.parse(res.data.schema || '[]')
      formId.value = res.data.id
    }
  } catch (error) {
    console.error('加载表单失败:', error)
  }
}

onMounted(() => {
  const id = props.id || route.params.id
  if (id) {
    loadForm(id)
  }
})
</script>

<style scoped>
.component-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  background: #f5f7fa;
  border-radius: 6px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.component-item:hover {
  background: #e6f0ff;
  color: #409eff;
}

.field-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.field-item:hover {
  border-color: #409eff;
}

.field-item.selected {
  border-color: #409eff;
  background: #ecf5ff;
}

.ghost {
  opacity: 0.5;
  background: #c8ebfb;
}

.property-panel {
  padding: 10px;
}

.drag-handle {
  color: #999;
}

.drag-handle:hover {
  color: #409eff;
}
</style>
