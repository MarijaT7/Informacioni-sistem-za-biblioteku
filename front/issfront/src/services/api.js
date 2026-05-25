import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' }
})


api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

export const authApi = {
  login:          (data)         => api.post('/auth/login', data),
  registerStep1:  (data)         => api.post('/auth/register/step1', data),
  registerStep2:  (jmbg, data)   => api.post(`/auth/register/step2/${jmbg}`, data),
  registerStep3:  (jmbg, data)   => api.post(`/auth/register/step3/${jmbg}`, data),
  saveGenres:     (jmbg, data)   => api.post(`/auth/register/genres/${jmbg}`, data),
  renewMembership: (jmbg, nacinUplate, tipPretplate) =>
    api.post(`/auth/renew/${jmbg}`, null, {
      params: { nacinUplate, tipPretplate }
    })


}
export const publicApi = {
  getLibraries:  () => api.get('/biblioteka'),
  getGenres:     () => api.get('/genres'),
  getKategorije: () => api.get('/kategorije'),
}
export const userApi = {
  getMe:             ()           => api.get('/users/me'),
  getProfile:        (jmbg)       => api.get(`/users/${jmbg}/profile`),
  updateProfile:     (jmbg, data) => api.put(`/users/${jmbg}/profile`, data),
  updateGenres:      (jmbg, data) => api.put(`/users/${jmbg}/genres`, data),
}
export default api
