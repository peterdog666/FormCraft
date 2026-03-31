<template>
  <div class="form-preview">
    <el-form :model="formData" label-width="100px">
      <el-form-item 
        v-for="field in schema" 
        :key="field.id"
        :label="field.label"
        :required="field.required"
      >
        <el-input 
          v-if="field.type === 'input'" 
          v-model="formData[field.id]" 
          :placeholder="field.placeholder"
        />
        <el-input 
          v-else-if="field.type === 'textarea'" 
          v-model="formData[field.id]" 
          type="textarea"
          :placeholder="field.placeholder"
        />
        <el-input-number 
          v-else-if="field.type === 'number'" 
          v-model="formData[field.id]" 
          :placeholder="field.placeholder"
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
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  schema: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['submit'])

const formData = ref({})

props.schema.forEach(field => {
  if (field.type === 'checkbox') {
    formData.value[field.id] = []
  } else {
    formData.value[field.id] = ''
  }
})

const handleSubmit = () => {
  emit('submit', formData.value)
}
</script>

<style scoped>
.form-preview {
  padding: 10px;
}
</style>
