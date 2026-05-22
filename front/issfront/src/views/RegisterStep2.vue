<template>
  <div class="page-center">
    <div class="card reg-card">
      <h1>Odaberite kategoriju</h1>

      <div class="step2-body">
        <!-- Category list -->
        <div class="category-list">
          <div
            v-for="kat in kategorije"
            :key="kat.idkc"
            class="radio-option"
            @click="selected = kat.idkc"
          >
            <div class="radio-circle" :class="{ active: selected === kat.idkc }"></div>
            <span class="radio-label">{{ labelOf(kat.tipKC) }}</span>
          </div>
        </div>

        <!-- Documentation panel -->
        <div class="doc-panel">
          <p class="doc-title">Neophodna dokumentacija za odabranu kategoriju:</p>
          <div class="doc-box">
            <span class="doc-text">{{ docText }}</span>
          </div>
        </div>
      </div>

      <p v-if="error" class="error-msg">{{ error }}</p>

      <button class="btn-primary" @click="handleNext" :disabled="loading || !selected">
        {{ loading ? 'Učitavanje…' : 'Nastavite' }}
      </button>

      <!-- Step navigation -->
      <div class="step-nav">
        <button class="step-btn" @click="router.push('/register')">‹</button>
        <button class="step-btn active">›</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stroage/auth.js'
import { authApi, publicApi } from '../services/api.js'

const router    = useRouter()
const authStore = useAuthStore()

const kategorije = ref([])
const selected   = ref(null)
const error      = ref('')
const loading    = ref(false)

onMounted(async () => {
  const res = await publicApi.getKategorije()
  kategorije.value = res.data
  if (res.data.length) selected.value = res.data[0].idkc
})

const LABELS = {
  REGULARNA: 'Regularna', DECIJA: 'Dečija',
  STUDENTSKA: 'Studentska', PENZIONERSKA: 'Penzionerska', PORODICNA: 'Porodična'
}
const DOCS = {
  REGULARNA:    'Za izabranu kategoriju nije potrebno priložiti nikakvu dokumentaciju',
  DECIJA:       'Potrebno je priložiti kopiju rodnog lista',
  STUDENTSKA:   'Potrebno je priložiti važeću studentsku legitimaciju',
  PENZIONERSKA: 'Potrebno je priložiti kopiju penzionerske kartice',
  PORODICNA:    'Potrebno je priložiti izvod iz matične knjige venčanih i rodni listi dece',
}

const labelOf = (tip) => LABELS[tip] || tip

const docText = computed(() => {
  const kat = kategorije.value.find(k => k.idkc === selected.value)
  return kat ? (DOCS[kat.tipKC] || '') : ''
})

async function handleNext() {
  error.value = ''
  loading.value = true
  try {
    await authApi.registerStep2(authStore.regJmbg, { kategorijaClanaId: selected.value })
    router.push('/register/step3')
  } catch (e) {
    error.value = e.response?.data?.message || 'Greška'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.reg-card { width: 100%; max-width: 700px; }
.step2-body {
  display: grid;
  grid-template-columns: 1fr 1.4fr;
  gap: 2rem;
  align-items: start;
}
.doc-title { font-size: 0.9rem; color: var(--text-mid); margin-bottom: 0.5rem; }
.doc-box {
  background: white;
  border-radius: 8px;
  padding: 1rem;
  min-height: 180px;
  font-size: 0.9rem;
  color: var(--text-mid);
}
.step-nav {
  display: flex;
  justify-content: center;
  gap: 0.5rem;
  margin-top: 1.5rem;
}
.step-btn {
  background: var(--btn-primary);
  color: white;
  border: none;
  border-radius: 6px;
  padding: 0.4rem 0.9rem;
  cursor: pointer;
  font-size: 1rem;
}
</style>
