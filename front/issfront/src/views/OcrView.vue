<template>
    <div class="app-layout">
        <SidebarNav />
    
        
        <main class="main-content">

            <div class="books-search">
                <input
                  v-model="query"
                  class="search-input"
                  type="search"
                  placeholder="Unesite naziv knjige"
                />
                <button class="btn-secondary" @click="runSearch">Pretraži</button>
              </div>

            <template>
                <p v-if="loading">Ucitavanje knjiga...</p>
                <p v-if="error" class="error-msg">{{ error }}</p>

                <div v-if="books.length" class="books-grid">
                  <article
                    v-for="book in books"
                    :key="book.isbn"
                    class="book-card"
                    @click="selectBook(book)"
                  >
                    
                    <div class="book-meta">
                      <h3>{{ book.title }}</h3>

                      <p>{{ book.author }}</p>

                      <small>
                        {{ book.recordId }}
                      </small>
                    </div>
                  </article>
                </div>

                <p v-else-if="!loading" class="empty-state">Nema rezultata za prikaz.</p>
              </template>
        </main>

    </div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import SidebarNav from '../components/Sidebar.vue'
import { useAuthStore } from '../stroage/auth.js'
import { searchApi } from '../services/api.js'

const authStore = useAuthStore()

const authorized = ref(false)

const query = ref('')
const books = ref([])

const loading = ref(false)
const error = ref('')

let searchTimer = null

onMounted(() => {
  const role = authStore.getRole()

  authorized.value =
    role === 'CLAN' ||
    role === 'BIBLIOTEKAR'

  runSearch()
})

watch(query, () => {
  if (searchTimer)
    clearTimeout(searchTimer)

  searchTimer = setTimeout(() => {
    runSearch()
  }, 300)
})

onBeforeUnmount(() => {
  if (searchTimer)
    clearTimeout(searchTimer)
})

async function runSearch() {
  console.log("runSearch called")

  loading.value = true
  error.value = ''

  try {
    const res = await searchApi.fulltext(query.value)

    console.log("response", res)
    console.log("data", res.data)

    books.value = res.data.content || res.data
  }
  catch (e) {
    console.error("search failed", e)
    console.error("response", e.response)
    error.value = e.response?.data || 'Greška'
  }
  finally {
    loading.value = false
  }
}

function selectBook(book) {
  console.log(book.recordId)
}
</script>
