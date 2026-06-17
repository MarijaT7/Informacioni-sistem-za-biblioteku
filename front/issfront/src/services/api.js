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

api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      const config = error.config || {}
      const isMediaRequest = config.responseType === 'blob'
      if (!isMediaRequest) {
        const auth = useAuthStore()
        auth.logout()
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

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
export const katalogApi = {
  svi: () => api.get('/katalog/all'),
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
  dodajKompletna:  (formData) =>
    api.post('/knjiga/nova/kompletna', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }),
  naslovna:        (isbn) => api.get(`/knjiga/naslovna/${isbn}`, { responseType: 'blob' }),
  pdf:             (isbn) => api.get(`/knjiga/eknjiga/${isbn}/pdf`, { responseType: 'blob' }),
  audio:           (isbn) => api.get(`/knjiga/audioknjiga/${isbn}/audio`, { responseType: 'blob' }),
  citanjeProgress: (isbn) => api.get(`/knjiga/eknjiga/${isbn}/progress`),
  sacuvajCitanje:  (isbn, data) => api.put(`/knjiga/eknjiga/${isbn}/progress`, data),
  slusanjeProgress: (isbn) => api.get(`/knjiga/audioknjiga/${isbn}/progress`),
  sacuvajSlusanje:  (isbn, data) => api.put(`/knjiga/audioknjiga/${isbn}/progress`, data),
  obrisi:          (isbn) => api.put(`/knjiga/delete/${isbn}`),
  obrisiEknjigu:   (isbn) => api.put(`/knjiga/${isbn}/brisanjeeknjige`),
  obrisiAudio:     (isbn) => api.put(`/knjiga/${isbn}/brisanjeaudioknjige`),
  azurirajKompletna: (isbn, formData) =>
    api.put(`/knjiga/${isbn}/kompletna`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }),
  preporucene:      ()             => api.get('/knjiga/preporucene'),
}

// ── Elektronske baze podataka ────────────────────────────────────────
export const bazePodatakaApi = {
  sveOsnovno: () => api.get('/baze-podataka/sve/osnovno'),
  pretraga: (q) => api.get('/baze-podataka/pretraga', { params: { q } }),
  detalji: (id) => api.get(`/baze-podataka/detalji/${id}`),
  preuzmi: (id) => api.get(`/baze-podataka/${id}/preuzmi`, { responseType: 'blob' }),
  kreiraj: (formData) =>
    api.post('/baze-podataka/nova', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }),
  azuriraj: (id, formData) =>
    api.put(`/baze-podataka/${id}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }),
  obrisi: (id) => api.delete(`/baze-podataka/${id}`),
}

// ── Izdavaci ─────────────────────────────────────────────────────────
export const izdavaciApi = {
  ispisiSve: () => api.get('/izdavaci/sve')
}
export const pozajmicaApi = {
  pozajmiFizicku:    (isbn)      => api.post(`/pozajmice/pozajmi/${isbn}`),
  pozajmiDigitalno:  (isbn, tip) => api.post(`/pozajmice/pozajmi-digitalno/${isbn}`, null, { params: { tip } }),
  mozePozajmiti:     ()          => api.get('/pozajmice/mozePozajmiti'),
  rezervisi:         (isbn)      => api.post(`/pozajmice/rezervisi/${isbn}`),
  getMoje:           ()          => api.get('/pozajmice/moje'),
  produzenje:        (idP)       => api.post(`/pozajmice/produzenje/${idP}`),
  izgubljena:        (idP)       => api.post(`/pozajmice/izgubljena/${idP}`),
  izRezervacije:     (idR)       => api.post(`/pozajmice/iz-rezervacije/${idR}`),
  getDostupno:       (isbn)      => api.get(`/pozajmice/dostupno/${isbn}`),
  imamPozajmicu:     (isbn)      => api.get(`/pozajmice/imam-pozajmicu/${isbn}`),
  getObavestenja:    ()          => api.get('/pozajmice/obavestenja'),
  markRead:          (idO)       => api.put(`/pozajmice/obavestenja/${idO}/procitano`),
  deleteObavestenje: (idO)       => api.delete(`/pozajmice/obavestenja/${idO}`),
}

// ── Predlozi za nabavku ──────────────────────────────────────────────
export const predlogApi = {
  kreiraj:          (data) => api.post('/predlozi/kreiraj', data),
  mojiPredlozi:     ()     => api.get('/predlozi/moji-zahtevi'),
  predloziNaCekanju: ()    => api.get('/predlozi/na-cekanju'),
  odobreniPredlozi: ()     => api.get('/predlozi/odobreni'),
  obradiPredlog:    (id, data) => api.patch(`/predlozi/obradi/${id}`, data),
  obradiPredlogMenadzer(id, odobren) {
    return api.put(`/predlozi/${id}/obrada-menadzer`, {
        odobren
    })
  }
}

// ── Notifikacije ─────────────────────── ───────────────────────────
export const notifikacijaApi = {
  mojeNotifikacije:    ()    => api.get('/notifikacije/moje'),
  oznаciKaoProcitanu:  (id)  => api.patch(`/notifikacije/procitana/${id}`),
  brojNeprocitanih:    ()    => api.get('/notifikacije/broj-neprocitanih'),
}

export default api
