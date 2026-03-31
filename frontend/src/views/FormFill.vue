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
          :rules="getFieldRules(field)"
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
import { ref, onMounted } from 'vue'
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

/**
 * 根据字段定义生成 Element Plus 校验规则
 * 同时支持旧版 field.required 和新版 field.validationRules
 */
const getFieldRules = (field) => {
  const rules = []
  const vr = field.validationRules || {}
  const trigger = ['checkbox', 'radio', 'select'].includes(field.type) ? 'change' : 'blur'

  // 必填
  const isRequired = vr.required !== undefined ? vr.required : field.required
  if (isRequired) {
    rules.push({
      required: true,
      message: vr.errorMsg || `请填写${field.label}`,
      trigger
    })
  }

  // 最小长度
  if (vr.minLength) {
    rules.push({
      min: Number(vr.minLength),
      message: vr.errorMsg || `最少需要 ${vr.minLength} 个字符`,
      trigger: 'blur'
    })
  }

  // 最大长度
  if (vr.maxLength) {
    rules.push({
      max: Number(vr.maxLength),
      message: vr.errorMsg || `最多允许 ${vr.maxLength} 个字符`,
      trigger: 'blur'
    })
  }

  // 模式校验
  if (vr.pattern) {
    const patternMap = {
      email:  { re: /^[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}$/, msg: '请输入有效的邮箱地址' },
      phone:  { re: /^1[3-9]\d{9}$/,                                         msg: '请输入有效的手机号码' },
      url:    { re: /^(https?|ftp):\/\/[^\s/$.?#].[^\s]*$/i,                 msg: '请输入有效的 URL 地址' },
      number: { re: /^-?\d+(\.\d+)?$/,                                        msg: '请输入有效的数字' }
    }
    if (vr.pattern === 'custom' && vr.customPattern) {
      rules.push({
        pattern: new RegExp(vr.customPattern),
        message: vr.errorMsg || '格式不正确',
        trigger: 'blur'
      })
    } else if (patternMap[vr.pattern]) {
      rules.push({
        pattern: patternMap[vr.pattern].re,
        message: vr.errorMsg || patternMap[vr.pattern].msg,
        trigger: 'blur'
      })
    }
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

      // 初始化表单值
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
    // 客户端校验
    await formRef.value.validate()
    submitting.value = true

    const formId = props.id || route.params.id
    const dataStr = JSON.stringify(formValues.value)

    await submissionApi.submit(formId, dataStr)

    ElMessage.success('提交成功！')

    // 重置表单
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
      // 服务端返回的校验错误已由 axios 拦截器处理，这里只处理非校验错误
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
