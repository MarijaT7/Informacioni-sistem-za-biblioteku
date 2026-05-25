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

// ── Knjige ───────────────────────────────────────────────────────────
export const knjigaApi = {
  sveOsnovno:      () => api.get('/knjiga/sve/osnovno'),
  pretraga:        (q) => api.get('/knjiga/pretraga', { params: { q } }),
  detalji:         (isbn) => api.get(`/knjiga/detalji/${isbn}`),
  naslovna:        (isbn) => api.get(`/knjiga/naslovna/${isbn}`, { responseType: 'blob' }),
  pdf:             (isbn) => api.get(`/knjiga/eknjiga/${isbn}/pdf`, { responseType: 'blob' }),
  audio:           (isbn) => api.get(`/knjiga/audioknjiga/${isbn}/audio`, { responseType: 'blob' }),
  citanjeProgress: (isbn) => api.get(`/knjiga/eknjiga/${isbn}/progress`),
  sacuvajCitanje:  (isbn, data) => api.put(`/knjiga/eknjiga/${isbn}/progress`, data),
  slusanjeProgress: (isbn) => api.get(`/knjiga/audioknjiga/${isbn}/progress`),
  sacuvajSlusanje:  (isbn, data) => api.put(`/knjiga/audioknjiga/${isbn}/progress`, data),
}

export default api
