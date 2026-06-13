<template>
  <div class="app-layout">
    <SidebarNav />

    <main class="main-content">
      <p v-if="!authorized" class="error-msg">Nemate pristup ovoj stranici.</p>

      <template v-else>
        <p v-if="loading">Ucitavanje detalja...</p>
        <p v-if="error" class="error-msg">{{ error }}</p>

        <div class="detail-top" v-if="book">
          <button class="btn-secondary" @click="backToList">Nazad na sve knjige</button>
        </div>

        <section v-if="book" class="book-detail">
          <div class="detail-cover">
            <img v-if="coverUrl" :src="coverUrl" alt="" />
            <div v-else class="cover-placeholder"></div>
          </div>

          <div class="detail-info">
            <h1>{{ book.naslov }}</h1>
            <p class="detail-author">{{ book.autor }}</p>

            <!-- CLAN VIEW: opis knjige + forma za pozajmicu -->
            <template v-if="isClan">
              <div class="format-icons">
                <div class="format-icon" :class="{ active: book.fizicka }" title="Fizička">
                  <span>📚</span><small>fizička</small>
                </div>
                <div class="format-icon" :class="{ active: book.elektronska }" title="Elektronska">
                  <span>📖</span><small>elektronska</small>
                </div>
                <div class="format-icon" :class="{ active: book.audio }" title="Audio">
                  <span>🎧</span><small>audio</small>
                </div>
              </div>

              <div class="detail-desc">
                <h3>Opis knjige</h3>
                <p>{{ book.sinopsis }}</p>
              </div>

              <!-- POZAJMICA SECTION -->
              <div class="borrow-section">
                <h3>Pozajmite knjigu:</h3>

                <!-- Physical book -->
                <div v-if="book.fizicka" class="borrow-row">
                  <span class="borrow-label">Fizička forma<span v-if="!dostupno"> - knjiga trenutno nije dostupna</span>:</span>
                  <button v-if="dostupno" class="btn-borrow" @click="pozajmiFizicku" :disabled="borrowing">
                    Pozajmite
                  </button>
                  <button v-else class="btn-borrow-secondary" @click="rezervisi" :disabled="reserving">
                    Rezervišite
                  </button>
                </div>

                <!-- E-book -->
                <div v-if="book.elektronska" class="borrow-row">
                  <span class="borrow-label">Elektronska forma:</span>
                  <button class="btn-borrow" @click="preuzmiEBook" :disabled="borrowing">
                    Preuzmite
                  </button>
                </div>

                <!-- Audio -->
                <div v-if="book.audio" class="borrow-row">
                  <span class="borrow-label">Audio forma:</span>
                  <button class="btn-borrow" @click="preuzmiAudio" :disabled="borrowing">
                    Preuzmite
                  </button>
                </div>

                <p v-if="book.elektronska || book.audio" class="borrow-note">
                  Preuzetim knjigama možete da pristupite iz opcije Pozajmice
                </p>
              </div>
            </template>

            <!-- LIBRARIAN VIEW -->
            <section v-if="isLibrarian" class="librarian-panel">
              <h3>Upravljanje knjigom</h3>
              <p class="helper-text">Izmenite osnovne podatke ili dodajte medije koji nedostaju.</p>

              <form class="librarian-form" @submit.prevent="saveChanges">
                <div class="form-row">
                  <div class="form-group">
                    <label for="book-title">Naslov</label>
                    <input id="book-title" v-model="form.naslov" type="text" />
                  </div>
                  <div class="form-group">
                    <label for="book-author">Autor</label>
                    <input id="book-author" v-model="form.autor" type="text" />
                  </div>
                  <div class="form-group">
                    <label for="book-catalog">Katalog</label>
                    <select id="book-catalog" v-model="form.katId" :disabled="catalogsLoading || !catalogs.length">
                      <option value="">Bez izmene</option>
                      <option v-for="catalog in catalogs" :key="catalog.katId" :value="catalog.katId">
                        {{ catalogLabel(catalog) }}
                      </option>
                    </select>
                  </div>
                </div>

                <div class="form-group">
                  <label for="book-sinopsis">Opis</label>
                  <textarea id="book-sinopsis" v-model="form.sinopsis" class="form-textarea" rows="4"></textarea>
                </div>

                <div class="media-grid">
                  <div class="media-card">
                    <div class="media-header">
                      <span class="media-title">eKnjiga (PDF)</span>
                      <span :class="book.elektronska ? 'status-pill status-ok' : 'status-pill status-missing'">
                        {{ book.elektronska ? 'Postoji' : 'Nedostaje' }}
                      </span>
                    </div>
                    <div class="media-actions" v-if="book.elektronska">
                      <button class="btn-danger-outline" type="button" :disabled="deletingEknjiga" @click="deleteEknjiga">Obriši eKnjigu</button>
                    </div>
                    <div v-if="!book.elektronska" class="form-group">
                      <label for="eknjiga-file">Dodaj PDF fajl</label>
                      <input id="eknjiga-file" type="file" accept="application/pdf,.pdf" @change="onPdfChange" />
                    </div>
                  </div>

                  <div class="media-card">
                    <div class="media-header">
                      <span class="media-title">Audio knjiga (MP3)</span>
                      <span :class="book.audio ? 'status-pill status-ok' : 'status-pill status-missing'">
                        {{ book.audio ? 'Postoji' : 'Nedostaje' }}
                      </span>
                    </div>
                    <div class="media-actions" v-if="book.audio">
                      <button class="btn-danger-outline" type="button" :disabled="deletingAudio" @click="deleteAudio">Obrisi audio</button>
                    </div>
                    <div v-if="!book.audio" class="form-group">
                      <label for="audioknjiga-file">Dodaj MP3 fajl</label>
                      <input id="audioknjiga-file" type="file" accept="audio/mpeg,audio/mp3,.mp3" @change="onMp3Change" />
                    </div>
                  </div>
                </div>

                <div class="form-actions">
                  <button class="btn-secondary" type="submit" :disabled="saving">Sačuvaj izmene</button>
                  <button class="btn-danger" type="button" :disabled="deleting" @click="deleteBook">Brisanje knjige</button>
                </div>

                <p v-if="saveError" class="error-msg">{{ saveError }}</p>
                <p v-if="saveSuccess" class="success-msg">{{ saveSuccess }}</p>
                <p v-if="mediaError" class="error-msg">{{ mediaError }}</p>
                <p v-if="mediaSuccess" class="success-msg">{{ mediaSuccess }}</p>
                <p v-if="deleteError" class="error-msg">{{ deleteError }}</p>
              </form>
            </section>
          </div>
        </section>
      </template>
    </main>

    <!-- MODAL for pozajmica result -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-box">
        <h2>{{ modalTitle }}</h2>
        <p>{{ modalMessage }}</p>
        <p v-if="modalExtra" class="modal-extra">{{ modalExtra }}</p>
        <button class="btn-secondary" @click="closeModal">Nazad na Početnu</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SidebarNav from '../components/Sidebar.vue'
import { useAuthStore } from '../stroage/auth.js'
import { katalogApi, knjigaApi, pozajmicaApi } from '../services/api.js'

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()

const isbn = computed(() => route.params.isbn)
const authorized = ref(false)
const book = ref(null)
const coverUrl = ref('')
const loading = ref(false)
const error = ref('')
const saving = ref(false)
const deleting = ref(false)
const saveError = ref('')
const saveSuccess = ref('')
const deleteError = ref('')
const mediaError = ref('')
const mediaSuccess = ref('')
const deletingEknjiga = ref(false)
const deletingAudio = ref(false)
const pdfFile = ref(null)
const mp3File = ref(null)
const catalogs = ref([])
const catalogsLoading = ref(false)
const catalogError = ref('')
const form = ref({
  naslov: '', autor: '', sinopsis: '', katId: '', brojStranaEK: '', trajanjeSekundeAK: ''
})

// Pozajmica state
const dostupno = ref(true)
const borrowing = ref(false)
const reserving = ref(false)
const showModal = ref(false)
const modalTitle = ref('')
const modalMessage = ref('')
const modalExtra = ref('')

const isClan = computed(() => authStore.getRole() === 'CLAN')
const isLibrarian = computed(() => authStore.getRole() === 'BIBLIOTEKAR')

onMounted(() => {
  const role = authStore.getRole()
  authorized.value = role === 'CLAN' || role === 'BIBLIOTEKAR'
  if (authorized.value) {
    loadDetails()
    if (role === 'BIBLIOTEKAR') loadCatalogs()
    if (role === 'CLAN') checkAvailability()
  }
})

onBeforeUnmount(() => {
  if (coverUrl.value) URL.revokeObjectURL(coverUrl.value)
})

async function checkAvailability() {
  try {
    const res = await pozajmicaApi.getDostupno(isbn.value)
    dostupno.value = res.data.dostupno
  } catch {
    dostupno.value = false
  }
}

async function loadDetails() {
  loading.value = true
  error.value = ''
  try {
    const res = await knjigaApi.detalji(isbn.value)
    book.value = res.data
    setFormFromBook()
    await loadCover()
  } catch (e) {
    error.value = e.response?.data || 'Greska pri ucitavanju.'
  } finally {
    loading.value = false
  }
}

async function loadCover() {
  try {
    const res = await knjigaApi.naslovna(isbn.value)
    coverUrl.value = URL.createObjectURL(res.data)
  } catch {
    coverUrl.value = ''
  }
}

async function pozajmiFizicku() {
  borrowing.value = true
  try {
    const res = await pozajmicaApi.pozajmiFizicku(isbn.value)
    const data = res.data
    modalTitle.value = 'Pozajmica'
    modalMessage.value = 'Knjiga je uspešno pozajmljena i biće dostupna za preuzimanje u vašoj odabranoj biblioteci od ' + formatDate(data.datPoz) + '.'
    modalExtra.value = 'Važenje pozajmice od ' + formatDate(data.datPoz) + ' do ' + formatDate(data.datOcVrac) + '.'
    showModal.value = true
    checkAvailability()
  } catch (e) {
    const msg = e.response?.data?.message || e.response?.data || 'Greška pri pozajmici.'
    modalTitle.value = 'Pozajmica'
    modalMessage.value = msg
    modalExtra.value = ''
    showModal.value = true
  } finally {
    borrowing.value = false
  }
}

async function rezervisi() {
  reserving.value = true
  try {
    const res = await pozajmicaApi.rezervisi(isbn.value)
    const data = res.data
    modalTitle.value = 'Rezervacija'
    modalMessage.value = 'Knjiga je uspešno rezervisana i očekuje se da će biti dostupna za preuzimanje u vašoj odabranoj biblioteci od ' + formatDate(data.datIspR) + '.'
    modalExtra.value = ''
    showModal.value = true
  } catch (e) {
    const msg = e.response?.data?.message || e.response?.data || 'Greška pri rezervaciji.'
    modalTitle.value = 'Rezervacija'
    modalMessage.value = msg
    modalExtra.value = ''
    showModal.value = true
  } finally {
    reserving.value = false
  }
}

async function preuzmiEBook() {
  borrowing.value = true
  try {
    const check = await pozajmicaApi.pozajmiDigitalno(isbn.value, 'ebook')
    if (check.data.success) {
      router.push(`/knjige/${isbn.value}/citaj`)
    } else {
      modalTitle.value = 'Pozajmica'
      modalMessage.value = check.data.message
      modalExtra.value = ''
      showModal.value = true
    }
  } catch (e) {
    const msg = e.response?.data?.message || e.response?.data || 'Greška.'
    modalTitle.value = 'Pozajmica'
    modalMessage.value = msg
    modalExtra.value = ''
    showModal.value = true
  } finally {
    borrowing.value = false
  }
}

async function preuzmiAudio() {
  borrowing.value = true
  try {
    const check = await pozajmicaApi.pozajmiDigitalno(isbn.value, 'audio')
    if (check.data.success) {
      router.push(`/knjige/${isbn.value}/slusaj`)
    } else {
      modalTitle.value = 'Pozajmica'
      modalMessage.value = check.data.message
      modalExtra.value = ''
      showModal.value = true
    }
  } catch (e) {
    const msg = e.response?.data?.message || e.response?.data || 'Greška.'
    modalTitle.value = 'Pozajmica'
    modalMessage.value = msg
    modalExtra.value = ''
    showModal.value = true
  } finally {
    borrowing.value = false
  }
}

function closeModal() {
  showModal.value = false
  router.push('/')
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${String(d.getDate()).padStart(2,'0')}. ${String(d.getMonth()+1).padStart(2,'0')}. ${d.getFullYear()}.`
}

function backToList() { router.push('/knjige') }

function setFormFromBook() {
  if (!book.value) return
  form.value.naslov = book.value.naslov || ''
  form.value.autor = book.value.autor || ''
  form.value.sinopsis = book.value.sinopsis || ''
  form.value.katId = ''
  form.value.brojStranaEK = book.value.brojStrana ?? ''
  form.value.trajanjeSekundeAK = ''
  pdfFile.value = null
  mp3File.value = null
  saveError.value = ''
  saveSuccess.value = ''
  mediaError.value = ''
  mediaSuccess.value = ''
  deleteError.value = ''
  syncCatalogSelection()
}

function onPdfChange(event) { pdfFile.value = event.target.files?.[0] || null }
function onMp3Change(event) { mp3File.value = event.target.files?.[0] || null }
function normalizeText(value) { const t = (value || '').trim(); return t.length ? t : null }
function normalizeInt(value) {
  if (value === '' || value == null) return null
  const p = Number(value)
  if (!Number.isFinite(p)) return null
  return Math.max(1, Math.floor(p))
}
function catalogLabel(catalog) {
  return catalog.standard ? `${catalog.katIme} (${catalog.standard})` : catalog.katIme
}
function syncCatalogSelection() {
  if (!book.value || !catalogs.value.length) return
  const match = catalogs.value.find(c => c.katIme === book.value.katalog)
  if (match) form.value.katId = match.katId
}

async function loadCatalogs() {
  if (!isLibrarian.value || catalogsLoading.value) return
  catalogsLoading.value = true
  catalogError.value = ''
  try {
    const res = await katalogApi.svi()
    catalogs.value = res.data || []
    syncCatalogSelection()
  } catch {
    catalogError.value = 'Greška pri učitavanju kataloga.'
  } finally {
    catalogsLoading.value = false
  }
}

async function saveChanges() {
  if (!isLibrarian.value || !book.value) return
  saving.value = true
  saveError.value = ''
  saveSuccess.value = ''
  try {
    const formData = new FormData()
    const podaci = {
      naslov: normalizeText(form.value.naslov),
      autor: normalizeText(form.value.autor),
      sinopsis: normalizeText(form.value.sinopsis),
      katId: form.value.katId || null,
      formatEK: book.value.elektronska ? 'PDF' : (pdfFile.value ? 'PDF' : null),
      brojStranaEK: normalizeInt(form.value.brojStranaEK),
      formatAK: book.value.audio ? 'MP3' : (mp3File.value ? 'MP3' : null),
      trajanjeSekundeAK: normalizeInt(form.value.trajanjeSekundeAK),
    }
    formData.append('podaci', new Blob([JSON.stringify(podaci)], { type: 'application/json' }))
    if (pdfFile.value) formData.append('pdf', pdfFile.value)
    if (mp3File.value) formData.append('mp3', mp3File.value)
    await knjigaApi.azurirajKompletna(isbn.value, formData)
    await loadDetails()
    saveSuccess.value = 'Knjiga uspesno azurirana.'
  } catch (e) {
    saveError.value = e.response?.data || 'Greska pri azuriranju.'
  } finally {
    saving.value = false
  }
}

async function deleteBook() {
  if (!isLibrarian.value || !book.value) return
  if (!window.confirm('Da li ste sigurni?')) return
  deleting.value = true
  try {
    await knjigaApi.obrisi(isbn.value)
    router.push('/knjige')
  } catch (e) {
    deleteError.value = e.response?.data || 'Greska pri brisanju.'
  } finally {
    deleting.value = false
  }
}

async function deleteEknjiga() {
  if (!window.confirm('Obrisati eKnjigu?')) return
  deletingEknjiga.value = true
  try {
    await knjigaApi.obrisiEknjigu(isbn.value)
    await loadDetails()
    mediaSuccess.value = 'eKnjiga uspesno obrisana.'
  } catch (e) {
    mediaError.value = e.response?.data || 'Greska pri brisanju eKnjige.'
  } finally {
    deletingEknjiga.value = false
  }
}

async function deleteAudio() {
  if (!window.confirm('Obrisati audio?')) return
  deletingAudio.value = true
  try {
    await knjigaApi.obrisiAudio(isbn.value)
    await loadDetails()
    mediaSuccess.value = 'Audio knjiga uspesno obrisana.'
  } catch (e) {
    mediaError.value = e.response?.data || 'Greska pri brisanju audio knjige.'
  } finally {
    deletingAudio.value = false
  }
}
</script>

<style scoped>
.book-detail {
  background: white;
  border-radius: 16px;
  padding: 1.5rem;
  display: grid;
  grid-template-columns: 180px 1fr;
  gap: 1.5rem;
  box-shadow: var(--shadow);
}
.detail-cover { width: 180px; height: 240px; background: #e3e3e3; border-radius: 12px; overflow: hidden; }
.detail-cover img { width: 100%; height: 100%; object-fit: cover; }
.cover-placeholder { width: 100%; height: 100%; background: linear-gradient(135deg, #c7c7c7, #b0b0b0); }
.detail-info h1 { margin: 0 0 0.3rem; text-align: left; font-size: 1.6rem; }
.detail-author { font-weight: 600; margin-bottom: 1rem; color: var(--text-mid); }
.detail-top { display: flex; justify-content: flex-start; margin-bottom: 1rem; }

.format-icons { display: flex; gap: 1rem; margin-bottom: 1rem; }
.format-icon { display: flex; flex-direction: column; align-items: center; gap: 0.2rem; padding: 0.5rem 0.8rem; border-radius: 10px; background: #f0ebe6; opacity: 0.4; font-size: 1.4rem; }
.format-icon.active { opacity: 1; background: #e8d8cd; }
.format-icon small { font-size: 0.7rem; color: var(--text-mid); }

.detail-desc { margin-bottom: 1.5rem; }
.detail-desc h3 { margin-bottom: 0.4rem; }
.detail-desc p { color: var(--text-mid); line-height: 1.5; }

.borrow-section h3 { margin-bottom: 0.75rem; }
.borrow-row { display: flex; align-items: center; gap: 1rem; margin-bottom: 0.75rem; flex-wrap: wrap; }
.borrow-label { font-size: 0.95rem; color: var(--text-dark); min-width: 200px; }
.borrow-note { font-size: 0.82rem; color: var(--text-mid); margin-top: 0.5rem; }
.btn-borrow {
  background: #7a5c48; color: #fff; border: none; border-radius: 50px;
  padding: 0.5rem 1.4rem; font-size: 0.9rem; cursor: pointer; transition: background 0.2s;
}
.btn-borrow:hover { background: #5e4436; }
.btn-borrow:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-borrow-secondary {
  background: transparent; color: #7a5c48; border: 1.5px solid #7a5c48;
  border-radius: 50px; padding: 0.5rem 1.4rem; font-size: 0.9rem; cursor: pointer;
}
.btn-borrow-secondary:hover { background: #f0ebe6; }

/* Modal */
.modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.45);
  display: flex; align-items: center; justify-content: center; z-index: 1000;
}
.modal-box {
  background: #f9f0ea; border-radius: 16px; padding: 2rem 2.5rem;
  max-width: 480px; width: 90%; text-align: center; box-shadow: 0 8px 30px rgba(0,0,0,0.2);
}
.modal-box h2 { margin-bottom: 1rem; font-size: 1.4rem; }
.modal-box p { color: var(--text-mid); line-height: 1.5; margin-bottom: 0.5rem; }
.modal-extra { font-weight: 600; color: var(--text-dark) !important; }
.modal-box .btn-secondary { margin-top: 1.2rem; }

/* Librarian */
.librarian-panel { margin-top: 1.5rem; padding-top: 1.5rem; border-top: 1px solid var(--border); text-align: left; }
.helper-text { color: var(--text-mid); margin-bottom: 1rem; }
.librarian-form { display: grid; gap: 1.25rem; }
.form-row { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 1rem; }
.form-textarea { background: var(--input-bg); border: 1.5px solid var(--border); border-radius: 6px; padding: 0.6rem 0.75rem; font-size: 0.95rem; color: var(--text-dark); resize: vertical; }
.media-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 1rem; }
.media-card { background: #fff; border-radius: 12px; padding: 1rem; box-shadow: var(--shadow); display: grid; gap: 0.8rem; }
.media-header { display: flex; align-items: center; justify-content: space-between; gap: 0.5rem; }
.media-title { font-weight: 600; }
.status-pill { padding: 0.2rem 0.6rem; border-radius: 999px; font-size: 0.8rem; }
.status-ok { background: #d8f1dd; color: #1d5a26; }
.status-missing { background: #f8d7d7; color: #7a1e1e; }
.media-actions { display: flex; justify-content: flex-end; }
.form-actions { display: flex; gap: 0.75rem; flex-wrap: wrap; }
.btn-danger { background: #b23b3b; color: #fff; border: none; border-radius: 50px; padding: 0.65rem 1.6rem; font-size: 1rem; cursor: pointer; }
.btn-danger:hover { background: #962f2f; }
.btn-danger:disabled { opacity: 0.7; cursor: not-allowed; }
.btn-danger-outline { background: transparent; color: #962f2f; border: 1px solid #c66a6a; border-radius: 50px; padding: 0.45rem 1.1rem; font-size: 0.9rem; cursor: pointer; }
.btn-danger-outline:hover { background: #f8d7d7; }
.success-msg { color: #1d5a26; background: #d8f1dd; border-radius: 6px; padding: 0.5rem 0.75rem; font-size: 0.9rem; margin-top: 0.5rem; text-align: center; }
@media (max-width: 900px) {
  .book-detail { grid-template-columns: 1fr; }
  .detail-cover { width: 100%; height: 260px; }
}
</style>
