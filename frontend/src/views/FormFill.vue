<template>
  <div class="form-fill-container">
    <div class="form-fill-content">
      <div class="form-header">
        <h1>{{ formData.name || '表单填写' }}</h1>
        <p v-if="formData.description">{{ formData.description }}</p>
      </div>

      <div v-if="loading" style="text-align: center; padding: 60px;">
        <el-icon class="is-loading" style="font-size: 40px;"><Loading /></el-icon>
      </div>

      <el-form 
        v-else-if="formData.schema && formData.schema.length > 0"
        ref="formRef"
        :model="formValues"
        label-width="120px"
        class="form-fill-form"
      >
        <el-form-item 
          v-for="field in formData.schema" 
          :key="field.id"
          :label="field.label"
          :prop="field.id"
          :rules="getRules(field)"
        >
          <el-input 
            v-if="field.type === 'input'" 
            v-model="formValues[field.id]" 
            :placeholder="field.placeholder"
            style="width: 100%;"
          />
          <el-input 
            v-else-if="field.type === 'textarea'" 
            v-model="formValues[field.id]" 
            type="textarea"
            :rows="4"
            :placeholder="field.placeholder"
            style="width: 100%;"
          />
          <el-input-number 
            v-else-if="field.type === 'number'" 
            v-model="formValues[field.id]" 
            :placeholder="field.placeholder"
            style="width: 100%;"
          />
          <el-date-picker 
            v-else-if="field.type === 'date'" 
            v-model="formValues[field.id]" 
            type="date"
            :placeholder="field.placeholder"
            style="width: 100%;"
          />
          <el-select 
            v-else-if="field.type === 'select'" 
            v-model="formValues[field.id]" 
            :placeholder="field.placeholder"
            style="width: 100%;"
          >
            <el-option 
              v-for="(opt, idx) in field.options" 
              :key="idx" 
              :label="opt" 
              :value="opt" 
            />
          </el-select>
          <el-radio-group 
            v-else-if="field.type === 'radio'" 
            v-model="formValues[field.id]"
          >
            <el-radio 
              v-for="(opt, idx) in field.options" 
              :key="idx" 
              :value="opt"
            >
              {{ opt }}
            </el-radio>
          </el-radio-group>
          <el-checkbox-group 
            v-else-if="field.type === 'checkbox'" 
            v-model="formValues[field.id]"
          >
            <el-checkbox 
              v-for="(opt, idx) in field.options" 
              :key="idx" 
              :value="opt"
            >
              {{ opt }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">提交</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <div v-else style="text-align: center; padding: 60px; color: #999;">
        暂无表单内容
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { formApi, submissionApi } from '../api/index.js'

const props = defineProps(['id'])
const route = useRoute()

const formData = ref({})
const formValues = ref({})
const formRef = ref(null)
const loading = ref(true)
const submitting = ref(false)

const getRules = (field) => {
  const rules = []
  if (field.required) {
    rules.push({ 
      required: true, 
      message: `请填写${field.label}`,
      trigger: field.type === 'checkbox' || field.type === 'radio' ? 'change' : 'blur'
    })
  }
  return rules
}

const loadForm = async () => {
  loading.value = true
  try {
    const formId = props.id || route.params.id
    const res = await formApi.getById(formId)
    if (res.data) {
      formData.value = {
        ...res.data,
        schema: JSON.parse(res.data.schema || '[]')
      }
      
      formData.value.schema.forEach(field => {
        if (field.type === 'checkbox') {
          formValues.value[field.id] = []
        } else {
          formValues.value[field.id] = ''
        }
      })
    }
  } catch (error) {
    console.error('加载表单失败:', error)
    ElMessage.error('加载表单失败')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    const formId = props.id || route.params.id
    const dataStr = JSON.stringify(formValues.value)
    
    await submissionApi.submit(formId, dataStr)
    
    ElMessage.success('提交成功！')
    
    const schema = formData.value.schema || []
    formValues.value = {}
    schema.forEach(field => {
      if (field.type === 'checkbox') {
        formValues.value[field.id] = []
      } else {
        formValues.value[field.id] = ''
      }
    })
    
    formRef.value.resetFields()
  } catch (error) {
    if (error !== false) {
      console.error('提交失败:', error)
    }
  } finally {
    submitting.value = false
  }
}

const handleReset = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

onMounted(() => {
  loadForm()
})
</script>

<style scoped>
.form-fill-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px;
}

.form-fill-content {
  max-width: 700px;
  margin: 0 auto;
  background: white;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
}

.form-header {
  text-align: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.form-header h1 {
  color: #333;
  margin-bottom: 10px;
}

.form-header p {
  color: #666;
}

.form-fill-form {
  margin-top: 20px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}
</style>
