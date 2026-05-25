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

            <div class="detail-actions">
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

            <div class="detail-desc">
              <h3>Opis</h3>
              <p>{{ book.sinopsis }}</p>
            </div>
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
import { knjigaApi } from '../services/api.js'

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()

const isbn = computed(() => route.params.isbn)
const authorized = ref(false)
const book = ref(null)
const coverUrl = ref('')
const loading = ref(false)
const error = ref('')

const isClan = computed(() => authStore.getRole() === 'CLAN')
const canRead = computed(() => isClan.value && book.value?.elektronska)
const canListen = computed(() => isClan.value && book.value?.audio)

onMounted(() => {
  const role = authStore.getRole()
  authorized.value = role === 'CLAN' || role === 'BIBLIOTEKAR'
  if (authorized.value) {
    loadDetails()
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
