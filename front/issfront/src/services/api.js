import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' }
})

// Attach JWT to every request
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// ── Auth ──────────────────────────────────────────────────────────────
export const authApi = {
  login:          (data)         => api.post('/auth/login', data),
  registerStep1:  (data)         => api.post('/auth/register/step1', data),
  registerStep2:  (jmbg, data)   => api.post(`/auth/register/step2/${jmbg}`, data),
  registerStep3:  (jmbg, data)   => api.post(`/auth/register/step3/${jmbg}`, data),
  saveGenres:     (jmbg, data)   => api.post(`/auth/register/genres/${jmbg}`, data),

}
export const publicApi = {
  getLibraries:  () => api.get('/biblioteka'),
  getGenres:     () => api.get('/genres'),
  getKategorije: () => api.get('/kategorije'),
}

// ── Dobavljači ────────────────────────────────────────────────────────
export const dobavljacApi = {
  kreiraj:       (data)      => api.post('/dobavljaci/unos', data),
  svi:           ()          => api.get('/dobavljaci/prikaz-svih'),
  jedan:         (id)        => api.get(`/dobavljaci/detaljan-prikaz/${id}`),
  izmeni:        (id, data)  => api.patch(`/dobavljaci/izmena/${id}`, data),
  obrisi:        (id)        => api.patch(`/dobavljaci/brisanje/${id}`),
}

// ── Ugovori ───────────────────────────────────────────────────────────
export const ugovorApi = {
  kreiraj:       (data)      => api.post('/ugovori/kreiranje', data),
  sviZaDobavljaca: (id)      => api.get(`/ugovori/ispisi-sve/${id}`),
  raskini:       (id)        => api.patch(`/ugovori/raskid/${id}`),
}

export default api
