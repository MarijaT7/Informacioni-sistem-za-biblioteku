<template>
  <div class="narudzbine-wrapper">
    <div class="page-header">
      <div>
        <h1>Narudžbine</h1>
        <p class="subtitle">Pregled svih narudžbina</p>
      </div>
      <RouterLink to="/menadzer/narudzbine/nova" class="btn-primary">
        + Nova narudžbina
      </RouterLink>
    </div>

    <div v-if="loading" class="state-msg">Učitavanje...</div>
    <div v-else-if="error" class="state-msg state-msg--error">{{ error }}</div>
    <div v-else-if="narudzbine.length === 0" class="state-msg">
      Nema narudžbina.
    </div>

    <div v-else class="table-wrapper">
      <table class="tabla">
        <thead>
          <tr>
            <th>ID</th>
            <th>Dobavljač</th>
            <th>Datum kreiranja</th>
            <th>Očekivana isporuka</th>
            <th>Ukupna cena</th>
            <th>Status</th>
            <th>Akcije</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="n in narudzbine" :key="n.id">
            <td>#{{ n.id }}</td>
            <td class="td-bold">{{ n.dobavljacNaziv }}</td>
            <td>{{ n.datumKreiranja }}</td>
            <td>{{ n.datumOcekivaneIsporuke }}</td>
            <td>{{ formatirajIznos(n.ukupnaCena) }}</td>
            <td>
              <span class="status-badge" :class="statusKlasa(n.status)">
                {{ statusNaziv(n.status) }}
              </span>
            </td>
            <td class="akcije">
              <RouterLink :to="`/menadzer/narudzbine/${n.id}`" class="btn-akcija btn-detalji">
                Detalji
              </RouterLink>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { narudzbinApi } from '../../services/api.js'

const narudzbine = ref([])
const loading = ref(false)
const error = ref('')

async function ucitaj() {
  loading.value = true
  error.value = ''
  try {
    const res = await narudzbinApi.getSve()
    narudzbine.value = res.data
  } catch (e) {
    error.value = e.response?.data?.message || 'Greška pri učitavanju narudžbina.'
  } finally {
    loading.value = false
  }
}

function formatirajIznos(iznos) {
  if (!iznos) return '0,00 RSD'
  return Number(iznos).toLocaleString('sr-RS', { minimumFractionDigits: 2 }) + ' RSD'
}

function statusKlasa(status) {
  if (status === 'KREIRANA') return 'status--kreirana'
  if (status === 'ISPORUCENA') return 'status--isporucena'
  if (status === 'REKLAMIRANA') return 'status--reklamirana'
  return ''
}

function statusNaziv(status) {
  if (status === 'KREIRANA') return 'Kreirana'
  if (status === 'ISPORUCENA') return 'Isporučena'
  if (status === 'REKLAMIRANA') return 'Reklamirana'
  return status
}

onMounted(ucitaj)
</script>

<style scoped>
.narudzbine-wrapper { max-width: 1000px; }

.page-header {
  display: flex; justify-content: space-between;
  align-items: flex-start; margin-bottom: 2rem;
}
.page-header h1 { margin: 0 0 0.25rem; font-size: 2rem; color: var(--text-h); }
.subtitle { color: var(--text); font-size: 0.95rem; margin: 0; }

.table-wrapper { border: 1px solid var(--border); border-radius: 12px; overflow: hidden; }
.tabla { width: 100%; border-collapse: collapse; font-size: 0.9rem; }
.tabla thead { background: var(--accent-bg); }
.tabla th {
  padding: 0.85rem 1.2rem; text-align: left; font-weight: 600;
  color: var(--accent); font-size: 0.78rem; text-transform: uppercase; letter-spacing: 0.05em;
}
.tabla td { padding: 0.85rem 1.2rem; border-top: 1px solid var(--border); color: var(--text-h); }
.tabla tbody tr:hover { background: var(--accent-bg); }
.td-bold { font-weight: 600; }

.status-badge { padding: 0.2rem 0.7rem; border-radius: 20px; font-size: 0.78rem; font-weight: 600; }
.status--kreirana { background: rgba(234,179,8,0.1); color: #b45309; }
.status--isporucena { background: rgba(34,197,94,0.1); color: #16a34a; }
.status--reklamirana { background: rgba(220,38,38,0.1); color: #dc2626; }
.status--otkazana { background: rgba(107,114,128,0.1); color: #6b7280; }

.akcije { display: flex; gap: 0.5rem; }
.btn-akcija {
  padding: 0.3rem 0.8rem; border-radius: 6px; font-size: 0.8rem;
  font-weight: 500; border: none; cursor: pointer; text-decoration: none;
  font-family: inherit; transition: opacity 0.15s; display: inline-block;
}
.btn-akcija:hover { opacity: 0.8; }
.btn-detalji { background: var(--accent-bg); color: var(--accent); }

.btn-primary {
  background: var(--accent); color: #fff; border: none; border-radius: 8px;
  padding: 0.6rem 1.2rem; font-size: 0.9rem; font-weight: 600;
  cursor: pointer; text-decoration: none; font-family: inherit; transition: opacity 0.15s;
  display: inline-block;
}
.btn-primary:hover { opacity: 0.85; }

.state-msg { text-align: center; padding: 3rem; color: var(--text); }
.state-msg--error { color: #dc2626; }
</style>