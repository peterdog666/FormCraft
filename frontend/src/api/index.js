import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use(
  config => config,
  error => Promise.reject(error)
)

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res
  },
  error => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export const formApi = {
  list: () => request.get('/form/list'),
  getById: (id) => request.get(`/form/${id}`),
  create: (data) => request.post('/form', data),
  update: (id, data) => request.put(`/form/${id}`, data),
  delete: (id) => request.delete(`/form/${id}`),
  publish: (id) => request.put(`/form/${id}/publish`),
  unpublish: (id) => request.put(`/form/${id}/unpublish`)
}

export const submissionApi = {
  submit: (formId, data) => request.post(`/submission/${formId}`, { data }),
  getList: (formId, page = 1, size = 10) => request.get(`/submission/${formId}?page=${page}&size=${size}`)
}

export default request
