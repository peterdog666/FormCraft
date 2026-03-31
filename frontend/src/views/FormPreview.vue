<template>
  <div class="form-preview">
    <el-form ref="formRef" :model="formData" label-width="100px">
      <el-form-item
        v-for="field in schema"
        :key="field.id"
        :label="field.label"
        :prop="field.id"
        :rules="getFieldRules(field)"
      >
        <el-input
          v-if="field.type === 'input'"
          v-model="formData[field.id]"
          :placeholder="field.placeholder"
          style="width: 100%;"
        />
        <el-input
          v-else-if="field.type === 'textarea'"
          v-model="formData[field.id]"
          type="textarea"
          :placeholder="field.placeholder"
          style="width: 100%;"
        />
        <el-input-number
          v-else-if="field.type === 'number'"
          v-model="formData[field.id]"
          :placeholder="field.placeholder"
          style="width: 100%;"
        />
        <el-date-picker
          v-else-if="field.type === 'date'"
          v-model="formData[field.id]"
          type="date"
          :placeholder="field.placeholder"
          style="width: 100%;"
        />
        <el-select
          v-else-if="field.type === 'select'"
          v-model="formData[field.id]"
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
          v-model="formData[field.id]"
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
          v-model="formData[field.id]"
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
    </el-form>
    <div style="text-align: center; margin-top: 20px;">
      <el-button type="primary" @click="handleSubmit">提交</el-button>
      <el-button @click="handleReset" style="margin-left: 10px;">重置</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  schema: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['submit'])

const formRef = ref(null)
const formData = ref({})

// 初始化表单数据
const initFormData = () => {
  const data = {}
  props.schema.forEach(field => {
    if (field.type === 'checkbox') {
      data[field.id] = []
    } else {
      data[field.id] = ''
    }
  })
  formData.value = data
}

initFormData()

// 当 schema 变化时重新初始化（用于实时预览场景）
watch(() => props.schema, () => {
  initFormData()
}, { deep: true })

/**
 * 根据字段定义生成 Element Plus 校验规则
 * 支持：required、minLength、maxLength、pattern（email/phone/url/number/custom）
 */
const getFieldRules = (field) => {
  const rules = []
  const vr = field.validationRules || {}
  const trigger = ['checkbox', 'radio', 'select'].includes(field.type) ? 'change' : 'blur'

  // 必填（优先使用 validationRules.required，其次 field.required）
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
      min: vr.minLength,
      message: vr.errorMsg || `最少需要 ${vr.minLength} 个字符`,
      trigger: 'blur'
    })
  }

  // 最大长度
  if (vr.maxLength) {
    rules.push({
      max: vr.maxLength,
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

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    emit('submit', { ...formData.value })
    ElMessage.success('校验通过')
  } catch {
    ElMessage.warning('请检查表单填写是否正确')
  }
}

const handleReset = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  initFormData()
}
</script>

<style scoped>
.form-preview {
  padding: 10px;
}
</style>
