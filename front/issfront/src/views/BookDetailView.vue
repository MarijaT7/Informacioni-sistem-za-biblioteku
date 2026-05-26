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
            <p class="detail-author">Autor: {{ book.autor }}</p>
            <p class="detail-katalog" v-if="book.katalog">
              Katalog: <strong>{{ book.katalog }}</strong>
            </p>

            <div class="detail-actions" v-if="isClan">
              <button
                class="btn-secondary"
                :disabled="!canRead"
                @click="openRead"
              >
                Čitajte e-knjigu
              </button>
              <button
                class="btn-secondary"
                :disabled="!canListen"
                @click="openListen"
              >
                Slušajte audio knjigu
              </button>
            </div>

            <div class="detail-desc" v-if="!isLibrarian">
              <h3>Opis</h3>
              <p>{{ book.sinopsis }}</p>
            </div>

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
                    <select
                      id="book-catalog"
                      v-model="form.katId"
                      :disabled="catalogsLoading || !catalogs.length"
                    >
                      <option value="">Bez izmene</option>
                      <option v-for="catalog in catalogs" :key="catalog.katId" :value="catalog.katId">
                        {{ catalogLabel(catalog) }}
                      </option>
                    </select>
                    <p v-if="catalogsLoading" class="hint-text">Učitavanje kataloga...</p>
                    <p v-if="catalogError" class="hint-text error-text">{{ catalogError }}</p>
                  </div>
                </div>

                <div class="form-group">
                  <label for="book-sinopsis">Opis</label>
                  <textarea
                    id="book-sinopsis"
                    v-model="form.sinopsis"
                    class="form-textarea"
                    rows="4"
                  ></textarea>
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
                      <button
                        class="btn-danger-outline"
                        type="button"
                        :disabled="deletingEknjiga"
                        @click="deleteEknjiga"
                      >
                        Obriši eKnjigu
                      </button>
                    </div>
                    <div v-if="!book.elektronska" class="form-group">
                      <label for="eknjiga-file">Dodaj PDF fajl</label>
                      <input
                        id="eknjiga-file"
                        type="file"
                        accept="application/pdf,.pdf"
                        @change="onPdfChange"
                      />
                    </div>
                    <div class="form-group">
                      <label for="eknjiga-pages">Broj strana (opciono): </label>
                      <input
                        id="eknjiga-pages"
                        v-model="form.brojStranaEK"
                        type="number"
                        min="1"
                        :disabled="!book.elektronska && !pdfFile"
                      />
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
                      <button
                        class="btn-danger-outline"
                        type="button"
                        :disabled="deletingAudio"
                        @click="deleteAudio"
                      >
                        Obrisi audio knjigu
                      </button>
                    </div>
                    <div v-if="!book.audio" class="form-group">
                      <label for="audioknjiga-file">Dodaj MP3 fajl</label>
                      <input
                        id="audioknjiga-file"
                        type="file"
                        accept="audio/mpeg,audio/mp3,.mp3"
                        @change="onMp3Change"
                      />
                    </div>
                    <div class="form-group">
                      <label for="audioknjiga-duration">Trajanje u sekundama (opciono): </label>
                      <input
                        id="audioknjiga-duration"
                        v-model="form.trajanjeSekundeAK"
                        type="number"
                        min="1"
                        :disabled="!book.audio && !mp3File"
                      />
                    </div>
                  </div>
                </div>

                <div class="form-actions">
                  <button class="btn-secondary" type="submit" :disabled="saving">
                    Sačuvaj izmene
                  </button>
                  <button class="btn-danger" type="button" :disabled="deleting" @click="deleteBook">
                    Brisanje knjige
                  </button>
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
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SidebarNav from '../components/Sidebar.vue'
import { useAuthStore } from '../stroage/auth.js'
import { katalogApi, knjigaApi } from '../services/api.js'

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
  naslov: '',
  autor: '',
  sinopsis: '',
  katId: '',
  brojStranaEK: '',
  trajanjeSekundeAK: ''
})

const isClan = computed(() => authStore.getRole() === 'CLAN')
const isLibrarian = computed(() => authStore.getRole() === 'BIBLIOTEKAR')
const canRead = computed(() => isClan.value && book.value?.elektronska)
const canListen = computed(() => isClan.value && book.value?.audio)

onMounted(() => {
  const role = authStore.getRole()
  authorized.value = role === 'CLAN' || role === 'BIBLIOTEKAR'
  if (authorized.value) {
    loadDetails()
    if (role === 'BIBLIOTEKAR') {
      loadCatalogs()
    }
  }
})

onBeforeUnmount(() => {
  if (coverUrl.value) URL.revokeObjectURL(coverUrl.value)
})

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

function openRead() {
  router.push(`/knjige/${isbn.value}/citaj`)
}

function openListen() {
  router.push(`/knjige/${isbn.value}/slusaj`)
}

function backToList() {
  router.push('/knjige')
}

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

function onPdfChange(event) {
  pdfFile.value = event.target.files?.[0] || null
}

function onMp3Change(event) {
  mp3File.value = event.target.files?.[0] || null
}

function normalizeText(value) {
  const trimmed = (value || '').trim()
  return trimmed.length ? trimmed : null
}

function normalizeInt(value) {
  if (value === '' || value == null) return null
  const parsed = Number(value)
  if (!Number.isFinite(parsed)) return null
  return Math.max(1, Math.floor(parsed))
}

function catalogLabel(catalog) {
  return catalog.standard ? `${catalog.katIme} (${catalog.standard})` : catalog.katIme
}

function syncCatalogSelection() {
  if (!book.value || !catalogs.value.length) return
  const match = catalogs.value.find((catalog) => catalog.katIme === book.value.katalog)
  if (match) {
    form.value.katId = match.katId
  }
}

async function loadCatalogs() {
  if (!isLibrarian.value || catalogsLoading.value) return
  catalogsLoading.value = true
  catalogError.value = ''
  try {
    const res = await katalogApi.svi()
    catalogs.value = res.data || []
    syncCatalogSelection()
  } catch (e) {
    catalogError.value = e.response?.data || 'Greska pri ucitavanju kataloga.'
  } finally {
    catalogsLoading.value = false
  }
}

async function saveChanges() {
  if (!isLibrarian.value || !book.value) return
  saving.value = true
  saveError.value = ''
  saveSuccess.value = ''
  mediaError.value = ''
  mediaSuccess.value = ''
  deleteError.value = ''
  try {
    const payload = {}
    const naslov = normalizeText(form.value.naslov)
    const autor = normalizeText(form.value.autor)
    const sinopsis = normalizeText(form.value.sinopsis)

    if (naslov != null) payload.naslov = naslov
    if (autor != null) payload.autor = autor
    if (sinopsis != null) payload.sinopsis = sinopsis

    const katId = normalizeInt(form.value.katId)
    if (katId != null) payload.katId = katId

    const brojStrana = normalizeInt(form.value.brojStranaEK)
    if (brojStrana != null && (book.value.elektronska || pdfFile.value)) {
      payload.brojStranaEK = brojStrana
    }

    const trajanjeSekunde = normalizeInt(form.value.trajanjeSekundeAK)
    if (trajanjeSekunde != null && (book.value.audio || mp3File.value)) {
      payload.trajanjeSekundeAK = trajanjeSekunde
    }

    const formData = new FormData()
    formData.append('podaci', new Blob([JSON.stringify(payload)], { type: 'application/json' }))
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
  const confirmed = window.confirm('Da li ste sigurni da zelite da obrisete ovu knjigu?')
  if (!confirmed) return
  deleting.value = true
  saveError.value = ''
  saveSuccess.value = ''
  mediaError.value = ''
  mediaSuccess.value = ''
  deleteError.value = ''
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
  if (!isLibrarian.value || !book.value?.elektronska) return
  const confirmed = window.confirm('Da li ste sigurni da želite da obrišete eKnjigu?')
  if (!confirmed) return
  deletingEknjiga.value = true
  mediaError.value = ''
  mediaSuccess.value = ''
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
  if (!isLibrarian.value || !book.value?.audio) return
  const confirmed = window.confirm('Da li ste sigurni da zelite da obrisete audio knjigu?')
  if (!confirmed) return
  deletingAudio.value = true
  mediaError.value = ''
  mediaSuccess.value = ''
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

.detail-cover {
  width: 180px;
  height: 240px;
  background: #e3e3e3;
  border-radius: 12px;
  overflow: hidden;
}

.detail-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #c7c7c7, #b0b0b0);
}

.detail-info h1 {
  margin: 0 0 0.4rem;
  text-align: left;
  font-size: 1.8rem;
}

.detail-author {
  font-weight: 600;
  margin-bottom: 0.6rem;
}

.detail-katalog {
  margin-bottom: 0.35rem;
  color: var(--text-mid);
}

.detail-actions {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
  margin: 1rem 0 1.5rem;
}

.detail-top {
  display: flex;
  justify-content: flex-start;
  margin-bottom: 1rem;
}

.detail-actions .btn-secondary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.detail-desc h3 {
  margin-bottom: 0.4rem;
}

.detail-desc p {
  color: var(--text-mid);
  line-height: 1.5;
}

.librarian-panel {
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--border);
  text-align: left;
}

.helper-text {
  color: var(--text-mid);
  margin-bottom: 1rem;
}

.librarian-form {
  display: grid;
  gap: 1.25rem;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1rem;
}

.form-textarea {
  background: var(--input-bg);
  border: 1.5px solid var(--border);
  border-radius: 6px;
  padding: 0.6rem 0.75rem;
  font-size: 0.95rem;
  color: var(--text-dark);
  resize: vertical;
}

.hint-text {
  font-size: 0.85rem;
  color: var(--text-mid);
  margin-top: 0.35rem;
}

.error-text {
  color: #7a1e1e;
}

.media-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 1rem;
}

.media-card {
  background: #fff;
  border-radius: 12px;
  padding: 1rem;
  box-shadow: var(--shadow);
  display: grid;
  gap: 0.8rem;
}

.media-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.5rem;
}

.media-title {
  font-weight: 600;
}

.status-pill {
  padding: 0.2rem 0.6rem;
  border-radius: 999px;
  font-size: 0.8rem;
}

.status-ok {
  background: #d8f1dd;
  color: #1d5a26;
}

.status-missing {
  background: #f8d7d7;
  color: #7a1e1e;
}

.media-actions {
  display: flex;
  justify-content: flex-end;
}

.form-actions {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.btn-danger {
  background: #b23b3b;
  color: #fff;
  border: none;
  border-radius: 50px;
  padding: 0.65rem 1.6rem;
  font-size: 1rem;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-danger:hover {
  background: #962f2f;
}

.btn-danger:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-danger-outline {
  background: transparent;
  color: #962f2f;
  border: 1px solid #c66a6a;
  border-radius: 50px;
  padding: 0.45rem 1.1rem;
  font-size: 0.9rem;
  cursor: pointer;
  transition: background 0.2s, color 0.2s, border-color 0.2s;
}

.btn-danger-outline:hover {
  background: #f8d7d7;
  color: #7a1e1e;
  border-color: #b23b3b;
}

.btn-danger-outline:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.success-msg {
  color: #1d5a26;
  background: #d8f1dd;
  border-radius: 6px;
  padding: 0.5rem 0.75rem;
  font-size: 0.9rem;
  margin-top: 0.5rem;
  text-align: center;
}

@media (max-width: 900px) {
  .book-detail {
    grid-template-columns: 1fr;
  }

  .detail-cover {
    width: 100%;
    height: 260px;
  }
}
</style>
