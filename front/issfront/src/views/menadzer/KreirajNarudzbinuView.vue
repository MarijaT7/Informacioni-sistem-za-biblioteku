<template>
  <div class="forma-wrapper">
    <div class="page-header">
      <h1>Narudžbine</h1>
      <p class="subtitle">Kreiranje nove narudžbine</p>
    </div>

    <div class="forma-kartica">
      <div v-if="error" class="alert alert--error">{{ error }}</div>
      <div v-if="uspeh" class="alert alert--uspeh">Narudžbina je uspešno kreirana!</div>

      <div class="form-group">
        <label>Dobavljač</label>
        <select v-model="forma.dobavljacId" @change="ucitajUgovor">
          <option :value="null" disabled>Izaberite dobavljača</option>
          <option v-for="d in dobavljaci" :key="d.id" :value="d.id">
            {{ d.naziv }}
          </option>
        </select>
      </div>

      <!-- Ugovor se učitava automatski -->
      <div v-if="aktivniUgovor" class="ugovor-info">
        <h3>Aktivni ugovor</h3>
        <div class="ugovor-grid">
          <div class="ugovor-row">
            <span class="ugovor-label">Popust</span>
            <span class="ugovor-value">{{ aktivniUgovor.popust }}%</span>
          </div>
          <div class="ugovor-row">
            <span class="ugovor-label">Rok isporuke</span>
            <span class="ugovor-value">{{ aktivniUgovor.rokIsporuke }} dan(a)</span>
          </div>
          <div class="ugovor-row">
            <span class="ugovor-label">Važi do</span>
            <span class="ugovor-value">{{ aktivniUgovor.datumIsteka }}</span>
          </div>
        </div>
      </div>

      <div v-if="forma.dobavljacId && !aktivniUgovor && !loadingUgovor" class="alert alert--error">
        Ovaj dobavljač nema aktivan ugovor.
      </div>

      <div class="form-group">
        <label>Napomena (opciono)</label>
        <textarea v-model="forma.napomena" rows="3" placeholder="Dodatne napomene..."></textarea>
      </div>

      <div class="forma-akcije">
        <button class="btn-sekundarni" @click="router.back()">Odustani</button>
        <button
          class="btn-primary"
          @click="sacuvaj"
          :disabled="loading || !aktivniUgovor">
          {{ loading ? 'Kreiranje...' : 'Kreiraj narudžbinu' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { narudzbinApi, dobavljacApi, ugovorApi } from '../../services/api.js'

const router = useRouter()

const dobavljaci = ref([])
const aktivniUgovor = ref(null)
const loadingUgovor = ref(false)
const loading = ref(false)
const error = ref('')
const uspeh = ref(false)

const forma = ref({
  dobavljacId: null,
  ugovorId: null,
  napomena: ''
})

async function ucitajDobavljace() {
  try {
    const res = await dobavljacApi.svi()
    dobavljaci.value = res.data.filter(d => d.status === 'AKTIVAN')
  } catch (e) {
    error.value = 'Greška pri učitavanju dobavljača.'
  }
}

async function ucitajUgovor() {
  aktivniUgovor.value = null
  forma.value.ugovorId = null
  if (!forma.value.dobavljacId) return

  loadingUgovor.value = true
  try {
    const res = await ugovorApi.sviZaDobavljaca(forma.value.dobavljacId)
    const aktivni = res.data.find(u => u.status === 'AKTIVAN')
    if (aktivni) {
      aktivniUgovor.value = aktivni
      forma.value.ugovorId = aktivni.id
    }
  } catch (e) {
    // tiho
  } finally {
    loadingUgovor.value = false
  }
}

async function sacuvaj() {
  error.value = ''
  uspeh.value = false

  if (!forma.value.dobavljacId || !forma.value.ugovorId) {
    error.value = 'Izaberite dobavljača sa aktivnim ugovorom.'
    return
  }

  loading.value = true
  try {
    const res = await narudzbinApi.kreiraj({
      dobavljacId: forma.value.dobavljacId,
      ugovorId: forma.value.ugovorId,
      napomena: forma.value.napomena
    })
    uspeh.value = true
    setTimeout(() => router.push(`/menadzer/narudzbine/${res.data.id}`), 1500)
  } catch (e) {
    error.value = e.response?.data?.message || 'Greška pri kreiranju narudžbine.'
  } finally {
    loading.value = false
  }
}

onMounted(ucitajDobavljace)
</script>

<style scoped>
.forma-wrapper { max-width: 600px; }

.page-header { margin-bottom: 2rem; }
.page-header h1 { margin: 0 0 0.25rem; font-size: 2rem; color: var(--text-h); }
.subtitle { color: var(--text); font-size: 0.95rem; margin: 0; }

.forma-kartica {
  border: 1px solid var(--border); border-radius: 16px;
  padding: 2rem; display: flex; flex-direction: column; gap: 1.25rem;
}

.form-group { display: flex; flex-direction: column; gap: 0.4rem; }
.form-group label { font-size: 0.9rem; color: var(--text); font-weight: 500; }
.form-group select,
.form-group textarea {
  padding: 0.65rem 1rem; border: 1px solid var(--border); border-radius: 8px;
  font-size: 0.95rem; color: var(--text-h); background: var(--bg);
  font-family: inherit; outline: none; transition: border-color 0.15s;
}
.form-group select:focus,
.form-group textarea:focus { border-color: var(--accent); }
.form-group textarea { resize: vertical; }

.ugovor-info {
  border: 1px solid var(--accent-border); background: var(--accent-bg);
  border-radius: 12px; padding: 1rem 1.25rem;
}
.ugovor-info h3 { margin: 0 0 0.75rem; color: var(--accent); font-size: 0.9rem; text-transform: uppercase; letter-spacing: 0.05em; }
.ugovor-grid { display: flex; flex-direction: column; gap: 0.4rem; }
.ugovor-row { display: flex; gap: 1rem; }
.ugovor-label { width: 120px; font-size: 0.85rem; color: var(--text); }
.ugovor-value { font-weight: 600; color: var(--text-h); font-size: 0.9rem; }

.forma-akcije { display: flex; justify-content: center; gap: 1rem; margin-top: 0.5rem; }

.btn-primary {
  background: var(--accent); color: #fff; border: none; border-radius: 8px;
  padding: 0.65rem 2rem; font-size: 0.95rem; font-weight: 600;
  cursor: pointer; font-family: inherit; transition: opacity 0.15s;
}
.btn-primary:hover:not(:disabled) { opacity: 0.85; }
.btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }

.btn-sekundarni {
  background: transparent; color: var(--text); border: 1px solid var(--border);
  border-radius: 8px; padding: 0.65rem 2rem; font-size: 0.95rem;
  font-weight: 500; cursor: pointer; font-family: inherit; transition: background 0.15s;
}
.btn-sekundarni:hover { background: var(--accent-bg); }

.alert { padding: 0.75rem 1rem; border-radius: 8px; font-size: 0.9rem; }
.alert--error { background: rgba(220,38,38,0.1); color: #dc2626; }
.alert--uspeh { background: rgba(34,197,94,0.1); color: #16a34a; }
</style>