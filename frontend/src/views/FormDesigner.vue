<template>
  <div class="designer-wrapper">
    <!-- 顶部工具栏 -->
    <div class="designer-toolbar">
      <el-input v-model="formName" placeholder="请输入表单名称" style="width: 280px;" />
      <el-button type="primary" @click="handleSave">保存</el-button>
      <el-button @click="handlePreviewDialog">预览</el-button>
      <el-button @click="handleBack">返回</el-button>
    </div>

    <!-- 四栏主体 -->
    <div class="designer-body">
      <!-- 左侧：组件面板 -->
      <div class="panel panel-components">
        <h4 class="panel-title">组件面板</h4>
        <div
          class="component-item"
          v-for="comp in components"
          :key="comp.type"
          @click="addComponent(comp)"
        >
          <el-icon><component :is="comp.icon" /></el-icon>
          <span>{{ comp.label }}</span>
        </div>
      </div>

      <!-- 中间：表单画布 -->
      <div class="panel panel-canvas">
        <h4 class="panel-title">表单画布</h4>
        <draggable
          v-model="formFields"
          item-key="id"
          handle=".drag-handle"
          ghost-class="ghost"
          @end="onDragEnd"
          class="canvas-drop-zone"
        >
          <template #item="{ element, index }">
            <div
              class="field-item"
              :class="{ selected: selectedField?.id === element.id }"
              @click="selectField(element)"
            >
              <el-icon class="drag-handle"><Rank /></el-icon>
              <div class="field-label">
                <span v-if="element.required || element.validationRules?.required" class="required-star">*</span>
                {{ element.label }}
                <span class="field-type-tag">{{ getTypeName(element.type) }}</span>
              </div>
              <el-button type="danger" link @click.stop="removeField(index)">删除</el-button>
            </div>
          </template>
        </draggable>
        <div v-if="formFields.length === 0" class="canvas-empty">
          从左侧点击组件添加字段
        </div>
      </div>

      <!-- 右侧：属性配置 -->
      <div class="panel panel-props">
        <h4 class="panel-title">属性配置</h4>
        <div v-if="selectedField" class="property-panel">
          <el-form label-width="70px" size="small">
            <!-- 基础属性 -->
            <div class="prop-section-title">基础属性</div>
            <el-form-item label="标签">
              <el-input v-model="selectedField.label" @input="updateField" />
            </el-form-item>
            <el-form-item label="占位符">
              <el-input v-model="selectedField.placeholder" @input="updateField" />
            </el-form-item>
            <el-form-item label="类型">
              <span class="type-badge">{{ getTypeName(selectedField.type) }}</span>
            </el-form-item>
            <el-form-item label="选项" v-if="hasOptions(selectedField.type)">
              <div
                v-for="(opt, idx) in selectedField.options"
                :key="idx"
                style="display: flex; gap: 5px; margin-bottom: 5px;"
              >
                <el-input
                  v-model="selectedField.options[idx]"
                  size="small"
                  style="flex: 1;"
                  @input="updateField"
                />
                <el-button
                  size="small"
                  @click="removeOption(idx)"
                  :disabled="selectedField.options.length <= 1"
                >-</el-button>
              </div>
              <el-button size="small" @click="addOption">+ 添加选项</el-button>
            </el-form-item>

            <!-- 校验规则 -->
            <div class="prop-section-title" style="margin-top: 12px;">校验规则</div>
            <el-form-item label="必填">
              <el-switch
                v-model="selectedField.validationRules.required"
                @change="updateField"
              />
            </el-form-item>
            <el-form-item label="最小长度" v-if="isTextType(selectedField.type)">
              <el-input-number
                v-model="selectedField.validationRules.minLength"
                :min="0"
                :max="9999"
                size="small"
                style="width: 100%;"
                @change="updateField"
              />
            </el-form-item>
            <el-form-item label="最大长度" v-if="isTextType(selectedField.type)">
              <el-input-number
                v-model="selectedField.validationRules.maxLength"
                :min="0"
                :max="9999"
                size="small"
                style="width: 100%;"
                @change="updateField"
              />
            </el-form-item>
            <el-form-item label="格式校验" v-if="isTextType(selectedField.type)">
              <el-select
                v-model="selectedField.validationRules.pattern"
                placeholder="不限"
                clearable
                size="small"
                style="width: 100%;"
                @change="updateField"
              >
                <el-option label="邮箱" value="email" />
                <el-option label="手机号" value="phone" />
                <el-option label="URL" value="url" />
                <el-option label="数字" value="number" />
                <el-option label="自定义正则" value="custom" />
              </el-select>
            </el-form-item>
            <el-form-item
              label="自定义正则"
              v-if="selectedField.validationRules.pattern === 'custom'"
            >
              <el-input
                v-model="selectedField.validationRules.customPattern"
                placeholder="如: ^[0-9]{11}$"
                size="small"
                @input="updateField"
              />
            </el-form-item>
            <el-form-item label="错误提示">
              <el-input
                v-model="selectedField.validationRules.errorMsg"
                placeholder="留空使用默认提示"
                size="small"
                @input="updateField"
              />
            </el-form-item>
          </el-form>
        </div>
        <div v-else class="props-empty">
          请选择一个字段进行配置
        </div>
      </div>

      <!-- 右下角：实时预览 -->
      <div class="panel panel-preview">
        <h4 class="panel-title">
          实时预览
          <span class="preview-badge">{{ formFields.length }} 个字段</span>
        </h4>
        <div class="preview-scroll">
          <div v-if="formFields.length === 0" class="preview-empty">
            添加字段后在此预览效果
          </div>
          <FormPreview
            v-else
            :schema="previewSchema"
            @submit="handlePreviewSubmit"
          />
        </div>
      </div>
    </div>

    <!-- 预览弹窗（点击"预览"按钮触发） -->
    <el-dialog v-model="previewVisible" title="表单预览" width="640px">
      <FormPreview :schema="previewSchema" @submit="handlePreviewSubmit" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import draggable from 'vuedraggable'
import { Rank } from '@element-plus/icons-vue'
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
  { type: 'input',    label: '单行输入', icon: 'Edit' },
  { type: 'textarea', label: '多行文本', icon: 'Document' },
  { type: 'number',   label: '数字输入', icon: 'Grid' },
  { type: 'date',     label: '日期选择', icon: 'Calendar' },
  { type: 'select',   label: '下拉选择', icon: 'List',         options: ['选项1', '选项2'] },
  { type: 'radio',    label: '单选',     icon: 'SuccessFilled', options: ['选项1', '选项2'] },
  { type: 'checkbox', label: '多选',     icon: 'Tickets',       options: ['选项1', '选项2'] }
]

const getTypeName = (type) => {
  const comp = components.find(c => c.type === type)
  return comp ? comp.label : type
}

const hasOptions = (type) => ['select', 'radio', 'checkbox'].includes(type)
const isTextType = (type) => ['input', 'textarea'].includes(type)

/** 实时预览用的 schema（深拷贝，避免预览组件修改影响画布） */
const previewSchema = computed(() =>
  JSON.parse(JSON.stringify(formFields.value))
)

/** 创建默认校验规则对象 */
const defaultValidationRules = () => ({
  required: false,
  minLength: null,
  maxLength: null,
  pattern: '',
  customPattern: '',
  errorMsg: ''
})

const addComponent = (comp) => {
  const newField = {
    id: Date.now() + '_' + Math.random().toString(36).substr(2, 9),
    type: comp.type,
    label: comp.label,
    placeholder: '',
    required: false,
    options: comp.options ? [...comp.options] : [],
    validationRules: defaultValidationRules()
  }
  formFields.value.push(newField)
  selectedField.value = newField
}

const selectField = (field) => {
  // 确保 validationRules 存在（兼容旧数据）
  if (!field.validationRules) {
    field.validationRules = defaultValidationRules()
    field.validationRules.required = !!field.required
  }
  selectedField.value = field
}

const updateField = () => {
  const index = formFields.value.findIndex(f => f.id === selectedField.value.id)
  if (index !== -1) {
    // 同步 required 字段（向后兼容）
    selectedField.value.required = !!selectedField.value.validationRules?.required
    formFields.value[index] = { ...selectedField.value }
    selectedField.value = formFields.value[index]
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

  // 构建校验规则 JSON
  const validationFields = formFields.value
    .filter(f => f.validationRules && Object.keys(f.validationRules).some(k => f.validationRules[k]))
    .map(f => ({ id: f.id, rules: f.validationRules }))

  const formData = {
    name: formName.value,
    description: '',
    schema: JSON.stringify(formFields.value),
    validationRules: validationFields.length > 0
      ? JSON.stringify({ fields: validationFields })
      : null
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

const handlePreviewDialog = () => {
  if (formFields.value.length === 0) {
    ElMessage.warning('请先添加字段')
    return
  }
  previewVisible.value = true
}

const handlePreviewSubmit = (data) => {
  console.log('预览提交:', data)
  ElMessage.success('表单校验通过（预览模式）')
}

const handleBack = () => {
  router.push('/')
}

const loadForm = async (id) => {
  try {
    const res = await formApi.getById(id)
    if (res.data) {
      formName.value = res.data.name
      const fields = JSON.parse(res.data.schema || '[]')

      // 解析已保存的校验规则，合并到字段中
      let rulesMap = {}
      if (res.data.validationRules) {
        try {
          const rulesRoot = JSON.parse(res.data.validationRules)
          if (rulesRoot.fields) {
            rulesRoot.fields.forEach(fr => {
              rulesMap[fr.id] = fr.rules
            })
          }
        } catch (e) {
          console.warn('解析校验规则失败', e)
        }
      }

      formFields.value = fields.map(f => ({
        ...f,
        validationRules: rulesMap[f.id] || defaultValidationRules()
      }))
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
/* ---- 整体布局 ---- */
.designer-wrapper {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f0f2f5;
  overflow: hidden;
}

.designer-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 20px;
  background: white;
  border-bottom: 1px solid #e4e7ed;
  flex-shrink: 0;
}

.designer-body {
  display: grid;
  grid-template-columns: 200px 1fr 260px 280px;
  grid-template-rows: 1fr;
  gap: 12px;
  padding: 12px;
  flex: 1;
  overflow: hidden;
}

/* ---- 通用面板 ---- */
.panel {
  background: white;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
}

.panel-title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  padding: 10px 14px;
  border-bottom: 1px solid #f0f0f0;
  margin: 0;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* ---- 组件面板 ---- */
.panel-components {
  overflow-y: auto;
}

.panel-components .panel-title {
  background: #f8f9fa;
}

.component-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 14px;
  cursor: pointer;
  font-size: 13px;
  transition: background 0.15s;
  border-bottom: 1px solid #f5f5f5;
}

.component-item:hover {
  background: #ecf5ff;
  color: #409eff;
}

/* ---- 画布 ---- */
.panel-canvas {
  overflow: hidden;
}

.canvas-drop-zone {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  min-height: 200px;
}

.canvas-empty {
  text-align: center;
  color: #c0c4cc;
  padding: 60px 20px;
  font-size: 13px;
}

.field-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.15s;
  background: white;
}

.field-item:hover {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64,158,255,0.1);
}

.field-item.selected {
  border-color: #409eff;
  background: #ecf5ff;
}

.field-label {
  flex: 1;
  font-size: 13px;
}

.required-star {
  color: #f56c6c;
  margin-right: 2px;
}

.field-type-tag {
  color: #909399;
  font-size: 11px;
  margin-left: 6px;
  background: #f0f2f5;
  padding: 1px 5px;
  border-radius: 3px;
}

.drag-handle {
  color: #c0c4cc;
  cursor: move;
  flex-shrink: 0;
}

.drag-handle:hover {
  color: #409eff;
}

.ghost {
  opacity: 0.4;
  background: #c8ebfb;
}

/* ---- 属性面板 ---- */
.panel-props {
  overflow-y: auto;
}

.property-panel {
  padding: 12px;
  overflow-y: auto;
}

.prop-section-title {
  font-size: 12px;
  font-weight: 600;
  color: #606266;
  padding: 4px 0 8px;
  border-bottom: 1px dashed #e4e7ed;
  margin-bottom: 10px;
}

.type-badge {
  font-size: 12px;
  background: #f0f2f5;
  padding: 2px 8px;
  border-radius: 4px;
  color: #606266;
}

.props-empty {
  color: #c0c4cc;
  text-align: center;
  padding: 40px 20px;
  font-size: 13px;
}

/* ---- 实时预览 ---- */
.panel-preview {
  overflow: hidden;
  border-left: 2px solid #409eff;
}

.panel-preview .panel-title {
  background: linear-gradient(135deg, #ecf5ff, #f0f9ff);
  color: #409eff;
}

.preview-badge {
  font-size: 11px;
  background: #409eff;
  color: white;
  padding: 1px 6px;
  border-radius: 10px;
  font-weight: normal;
}

.preview-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.preview-empty {
  text-align: center;
  color: #c0c4cc;
  padding: 40px 20px;
  font-size: 12px;
}

/* ---- 响应式：小屏幕折叠预览 ---- */
@media (max-width: 1400px) {
  .designer-body {
    grid-template-columns: 180px 1fr 240px 240px;
  }
}

@media (max-width: 1100px) {
  .designer-body {
    grid-template-columns: 160px 1fr 220px;
    grid-template-rows: 1fr 1fr;
  }
  .panel-preview {
    grid-column: 1 / -1;
    grid-row: 2;
    max-height: 300px;
  }
}
</style>
