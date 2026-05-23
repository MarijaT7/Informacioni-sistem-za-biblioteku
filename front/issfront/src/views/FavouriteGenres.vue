<template>
  <div class="app-layout">
    <!-- Sidebar -->
    <aside class="sidebar">
      <div class="sidebar-profile">
        <div class="sidebar-avatar">👤</div>
        <div class="sidebar-name">{{ authStore.user?.firstName }} {{ authStore.user?.lastName }}</div>
      </div>
      <nav class="sidebar-nav">
        <RouterLink class="nav-item" to="/profile"><span class="nav-icon">🏠</span> Početna</RouterLink>
        <RouterLink class="nav-item" to="/profile"><span class="nav-icon">📖</span> Sve knjige</RouterLink>
        <RouterLink class="nav-item" to="/profile"><span class="nav-icon">🤚</span> Pozajmice</RouterLink>
        <RouterLink class="nav-item" to="/profile"><span class="nav-icon">🛍</span> Dugovanja</RouterLink>
        <RouterLink class="nav-item" to="/profile"><span class="nav-icon">🔔</span> Obaveštenja</RouterLink>
      </nav>
    </aside>

    <!-- Main content -->
    <main class="main-content">
      <h1 style="text-align:left;margin-bottom:0.3rem">Odaberite omiljene žanrove</h1>
      <p class="subtitle">Omiljene žanrove uvek možete da promenite iz opcije Moj nalog</p>

      <div class="genre-grid">
        <div
          v-for="genre in genres"
          :key="genre.id"
          class="radio-option"
          @click="toggle(genre.id)"
        >
          <div class="radio-circle" :class="{ active: selected.has(genre.id) }"></div>
          <span class="radio-label">{{ genre.name }}</span>
        </div>
      </div>

      <p v-if="error" class="error-msg">{{ error }}</p>

      <div style="display:flex; justify-content:flex-end; margin-top:2rem">
        <button class="btn-primary" style="margin:0" @click="handleNext" :disabled="loading">
          {{ loading ? 'Čuvanje…' : 'Dalje' }}
        </button>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stroage/auth.js'
import { authApi, publicApi } from '../services/api.js'

const router    = useRouter()
const authStore = useAuthStore()

const genres   = ref([])
const selected = ref(new Set())
const error    = ref('')
const loading  = ref(false)

onMounted(async () => {
  const res = await publicApi.getGenres()
  genres.value = res.data
})

function toggle(id) {
  if (selected.value.has(id)) selected.value.delete(id)
  else selected.value.add(id)
}

async function handleNext() {
  loading.value = true
  try {
    await authApi.saveGenres(authStore.regJmbg, { genreIds: [...selected.value] })
    router.push('/profile')
  } catch (e) {
    error.value = e.response?.data?.message || 'Greška'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.subtitle { color: var(--text-mid); font-size: 0.9rem; margin-bottom: 1.5rem; }
.genre-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.4rem 2rem;
}
</style>
