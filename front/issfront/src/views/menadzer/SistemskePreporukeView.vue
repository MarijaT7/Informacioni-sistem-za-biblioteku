<template>
  <div class="preporuke-wrapper">
    <div class="page-header">
      <div class="header-tekst">
        <h1>Sistemske preporuke</h1>
        <p class="subtitle">Naslovi sa naglim porastom potražnje, prepoznati automatskom analizom</p>
      </div>
      <button type="button" class="btn-pokreni" @click="pokreniAnalizu" :disabled="analizaUTo">
        <span v-if="analizaUTo" class="btn-spinner"></span>
        {{ analizaUTo ? 'Analiza u toku...' : '🔍 Pokreni analizu' }}
      </button>
    </div>

    <p v-if="poruka" class="poruka-info">{{ poruka }}</p>

    <div v-if="loading" class="state-msg">
      <div class="spinner"></div>
      <p>Učitavanje preporuka...</p>
    </div>
    <div v-else-if="error" class="state-msg state-msg--error">
      <span class="state-icon">❌</span>
      <p>{{ error }}</p>
    </div>
    <div v-else-if="preporuke.length === 0" class="state-msg state-msg--empty">
      <span class="state-icon">📈</span>
      <p>Trenutno nema aktivnih preporuka. Pokreni analizu da proveriš najnovije trendove.</p>
    </div>

    <div v-else class="table-wrapper animated-fade-in">
      <table class="tabla">
        <thead>
          <tr>
            <th>Naslov</th>
            <th>Autor</th>
            <th>Broj primeraka</th>
            <th>Broj pozajmica</th>
            <th>Predlog</th>
            <th>Datum generisanja</th>
            <th>Akcije</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in preporuke" :key="p.id">
            <td class="td-naslov">{{ p.naslov }}</td>
            <td class="td-autor">{{ p.autor }}</td>
            <td>{{ p.trenutniBrojPrimeraka }}</td>
            <td>{{ p.brojPozajmica }}</td>
            <td class="td-predlog">{{ p.predlog }}</td>
            <td class="td-datum">📅 {{ formatDatum(p.datumGenerisanja) }}</td>
            <td class="td-akcije">
              <button type="button" class="btn-akcija btn-prihvati" :disabled="obradaUTokuId === p.id" @click="obradi(p, 'PRIHVACENO')">
                ✓ Prihvati
              </button>
              <button type="button" class="btn-akcija btn-ignorisi" :disabled="obradaUTokuId === p.id" @click="obradi(p, 'IGNORISANO')">
                ✕ Ignorisi
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { sistemskePreporukeApi } from '../../services/api.js'

const preporuke = ref([])
const loading = ref(false)
const error = ref('')
const poruka = ref('')
const analizaUTo = ref(false)
const obradaUTokuId = ref(null)

async function ucitaj() {
  loading.value = true
  error.value = ''
  try {
    const res = await sistemskePreporukeApi.getAktivne()
    preporuke.value = res.data
  } catch (e) {
    error.value = e.response?.data?.message || 'Greška pri učitavanju preporuka.'
  } finally {
    loading.value = false
  }
}

async function pokreniAnalizu() {
  analizaUTo.value = true
  poruka.value = ''
  error.value = ''
  try {
    await sistemskePreporukeApi.pokreniAnalizu()
    await ucitaj()
    poruka.value = 'Analiza je završena, lista je ažurirana.'
  } catch (e) {
    error.value = e.response?.data?.message || 'Greška pri pokretanju analize.'
  } finally {
    analizaUTo.value = false
  }
}

async function obradi(preporuka, noviStatus) {
  obradaUTokuId.value = preporuka.id
  error.value = ''
  try {
    await sistemskePreporukeApi.azurirajStatus(preporuka.id, noviStatus)
    preporuke.value = preporuke.value.filter(p => p.id !== preporuka.id)
  } catch (e) {
    error.value = e.response?.data?.message || 'Greška pri ažuriranju preporuke.'
  } finally {
    obradaUTokuId.value = null
  }
}

function formatDatum(datum) {
  if (!datum) return ''
  return new Date(datum).toLocaleDateString('sr-RS')
}

onMounted(ucitaj)
</script>

<style scoped>
.preporuke-wrapper {
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.page-header {
  margin-bottom: 2rem;
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
}
.page-header h1 {
  margin: 0 0 0.35rem;
  font-size: 2.25rem;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: #3f4e37;
}
.subtitle {
  color: #556644;
  font-size: 1rem;
  margin: 0;
  opacity: 0.85;
}

.btn-pokreni {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.25rem;
  border: none;
  border-radius: 10px;
  background: #7a8f6e;
  color: #ffffff;
  font-weight: 600;
  font-size: 0.9rem;
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.15s ease;
}
.btn-pokreni:hover:not(:disabled) { background: #688060; }
.btn-pokreni:disabled { opacity: 0.7; cursor: not-allowed; }

.btn-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255,255,255,0.4);
  border-top-color: #ffffff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.poruka-info {
  background: rgba(34, 197, 94, 0.1);
  color: #15803d;
  padding: 0.75rem 1.25rem;
  border-radius: 10px;
  margin-bottom: 1.5rem;
  font-size: 0.9rem;
  font-weight: 500;
}

.table-wrapper {
  background: #ffffff !important;
  border: none;
  border-radius: 20px;
  overflow: hidden;
  width: 100%;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.06);
}

.tabla { width: 100%; border-collapse: collapse; font-size: 0.95rem; text-align: left; }
.tabla thead { background: #f4f6f0; }
.tabla th {
  padding: 1.1rem 1.5rem;
  font-weight: 600;
  color: #3f4e37;
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  border-bottom: 1px solid #eef0ea;
}
.tabla td {
  padding: 1.1rem 1.5rem;
  border-top: 1px solid #f4f6f0;
  color: #333333;
  vertical-align: middle;
}
.tabla tbody tr { transition: background-color 0.15s ease; }
.tabla tbody tr:hover { background: #f9faf7; }

.td-naslov { font-weight: 600; color: #3f4e37; }
.td-autor { color: #666666; }
.td-predlog { color: #444444; }
.td-datum { color: #666666; font-size: 0.9rem; white-space: nowrap; }

.td-akcije { display: flex; gap: 0.5rem; }
.btn-akcija {
  padding: 0.4rem 0.85rem;
  border-radius: 50px;
  border: none;
  font-size: 0.8rem;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.15s ease;
}
.btn-akcija:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-prihvati { background: rgba(34, 197, 94, 0.1); color: #15803d; }
.btn-prihvati:hover:not(:disabled) { background: rgba(34, 197, 94, 0.2); }
.btn-ignorisi { background: rgba(239, 68, 68, 0.1); color: #b91c1c; }
.btn-ignorisi:hover:not(:disabled) { background: rgba(239, 68, 68, 0.2); }

.state-msg {
  text-align: center;
  padding: 4rem 2rem;
  background: #ffffff;
  border-radius: 20px;
  box-shadow: 0 4px 18px rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  width: 100%;
  box-sizing: border-box;
}
.state-msg p { margin: 0; font-size: 1.05rem; font-weight: 500; color: #556644; }
.state-icon { font-size: 2.5rem; }
.state-msg--error { color: #dc2626; }

.spinner {
  width: 30px;
  height: 30px;
  border: 3px solid #e2e8f0;
  border-top-color: #7a8f6e;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.animated-fade-in { animation: fadeIn 0.35s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }
</style>