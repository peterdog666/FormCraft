import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

request.interceptors.request.use(
  config => config,
  error => Promise.reject(error)
)

request.interceptors.response.use(
  response => {
    // 如果是二进制流（Excel 下载），直接返回 response
    if (response.config.responseType === 'blob') {
      return response
    }
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res
  },
  error => {
    // 处理服务端返回的 400 校验错误
    if (error.response && error.response.data) {
      const data = error.response.data
      const msg = data.msg || data.message || '请求失败'
      ElMessage.error(msg)
    } else {
      ElMessage.error(error.message || '网络错误')
    }
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
  getList: (formId, page = 1, size = 10) =>
    request.get(`/submission/${formId}?page=${page}&size=${size}`),

  /**
   * 导出 Excel
   * @param {number} formId
   * @param {string|null} startDate  格式 YYYY-MM-DD
   * @param {string|null} endDate    格式 YYYY-MM-DD
   */
  exportExcel: (formId, startDate, endDate) => {
    const params = new URLSearchParams()
    if (startDate) params.append('startDate', startDate)
    if (endDate) params.append('endDate', endDate)
    const query = params.toString() ? `?${params.toString()}` : ''
    return request.get(`/submission/${formId}/export${query}`, {
      responseType: 'blob',
      timeout: 60000
    })
  }
}

export default request
