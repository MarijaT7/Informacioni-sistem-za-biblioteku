<template>
  <div class="izvestaji-wrapper">
    <div class="page-header">
      <h1>Izveštaji</h1>
      <p class="subtitle">Generisanje PDF izveštaja</p>
    </div>

    <div class="izvestaji-grid">

      <!-- Izvestaj o aktivnostima -->
      <div class="izvestaj-kartica">
        <div class="kartica-ikona">📊</div>
        <h2>Izveštaj o aktivnostima</h2>
        <p class="kartica-opis">Pozajmice, aktivnost članova, popularnost naslova i trendovi čitanja.</p>

        <div v-if="aktivnostiError" class="alert alert--error">{{ aktivnostiError }}</div>
        <div v-if="aktivnostiUspeh" class="alert alert--uspeh">PDF je preuzet!</div>

        <div class="forma">
          <div class="form-group">
            <label>Od datuma</label>
            <input v-model="aktivnostiForma.od" type="date" />
          </div>
          <div class="form-group">
            <label>Do datuma</label>
            <input v-model="aktivnostiForma.do" type="date" />
          </div>
        </div>

        <button
          class="btn-primary"
          @click="preuzmiAktivnosti"
          :disabled="loadingAktivnosti">
          {{ loadingAktivnosti ? 'Generisanje...' : '⬇ Preuzmi PDF' }}
        </button>
      </div>

      <!-- Izvestaj o nabavci -->
      <div class="izvestaj-kartica">
        <div class="kartica-ikona">💰</div>
        <h2>Izveštaj o nabavci</h2>
        <p class="kartica-opis">Troškovi nabavke, popunjenost fondova i zadovoljenje potreba korisnika.</p>

        <div v-if="nabavkaError" class="alert alert--error">{{ nabavkaError }}</div>
        <div v-if="nabavkaUspeh" class="alert alert--uspeh">PDF je preuzet!</div>

        <div class="forma">
          <div class="form-group">
            <label>Od datuma</label>
            <input v-model="nabavkaForma.od" type="date" />
          </div>
          <div class="form-group">
            <label>Do datuma</label>
            <input v-model="nabavkaForma.do" type="date" />
          </div>
        </div>

        <button
          class="btn-primary"
          @click="preuzmiNabavku"
          :disabled="loadingNabavka">
          {{ loadingNabavka ? 'Generisanje...' : '⬇ Preuzmi PDF' }}
        </button>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { izvestajApi } from '../../services/api.js'

const aktivnostiForma = ref({ od: '', do: '' })
const nabavkaForma = ref({ od: '', do: '' })

const loadingAktivnosti = ref(false)
const loadingNabavka = ref(false)
const aktivnostiError = ref('')
const nabavkaError = ref('')
const aktivnostiUspeh = ref(false)
const nabavkaUspeh = ref(false)

async function preuzmiAktivnosti() {
  aktivnostiError.value = ''
  aktivnostiUspeh.value = false

  if (!aktivnostiForma.value.od || !aktivnostiForma.value.do) {
    aktivnostiError.value = 'Oba datuma su obavezna.'
    return
  }
  if (aktivnostiForma.value.od > aktivnostiForma.value.do) {
    aktivnostiError.value = 'Datum "Od" mora biti pre datuma "Do".'
    return
  }

  loadingAktivnosti.value = true
  try {
    const res = await izvestajApi.aktivnosti(aktivnostiForma.value.od, aktivnostiForma.value.do)
    preuzmiBlob(res.data, `izvestaj-aktivnosti-${aktivnostiForma.value.od}.pdf`)
    aktivnostiUspeh.value = true
    setTimeout(() => { aktivnostiUspeh.value = false }, 3000)
  } catch (e) {
    aktivnostiError.value = 'Greška pri generisanju izveštaja.'
  } finally {
    loadingAktivnosti.value = false
  }
}

async function preuzmiNabavku() {
  nabavkaError.value = ''
  nabavkaUspeh.value = false

  if (!nabavkaForma.value.od || !nabavkaForma.value.do) {
    nabavkaError.value = 'Oba datuma su obavezna.'
    return
  }
  if (nabavkaForma.value.od > nabavkaForma.value.do) {
    nabavkaError.value = 'Datum "Od" mora biti pre datuma "Do".'
    return
  }

  loadingNabavka.value = true
  try {
    const res = await izvestajApi.nabavka(nabavkaForma.value.od, nabavkaForma.value.do)
    preuzmiBlob(res.data, `izvestaj-nabavka-${nabavkaForma.value.od}.pdf`)
    nabavkaUspeh.value = true
    setTimeout(() => { nabavkaUspeh.value = false }, 3000)
  } catch (e) {
    nabavkaError.value = 'Greška pri generisanju izveštaja.'
  } finally {
    loadingNabavka.value = false
  }
}

function preuzmiBlob(blob, filename) {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}
</script>

<style scoped>
.izvestaji-wrapper { max-width: 900px; }

.page-header { margin-bottom: 2rem; }
.page-header h1 { margin: 0 0 0.25rem; font-size: 2rem; color: var(--text-h); }
.subtitle { color: var(--text); font-size: 0.95rem; margin: 0; }

.izvestaji-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}

.izvestaj-kartica {
  border: 1px solid var(--border); border-radius: 16px;
  padding: 2rem; display: flex; flex-direction: column; gap: 1rem;
}

.kartica-ikona { font-size: 2.5rem; }

.izvestaj-kartica h2 { margin: 0; color: var(--text-h); font-size: 1.2rem; }

.kartica-opis { color: var(--text); font-size: 0.9rem; margin: 0; line-height: 1.5; }

.forma { display: flex; flex-direction: column; gap: 0.75rem; }

.form-group { display: flex; flex-direction: column; gap: 0.4rem; }
.form-group label { font-size: 0.85rem; color: var(--text); font-weight: 500; }
.form-group input {
  padding: 0.6rem 1rem; border: 1px solid var(--border); border-radius: 8px;
  font-size: 0.9rem; color: var(--text-h); background: var(--bg);
  font-family: inherit; outline: none; transition: border-color 0.15s;
}
.form-group input:focus { border-color: var(--accent); }

.btn-primary {
  background: var(--accent); color: #fff; border: none; border-radius: 8px;
  padding: 0.7rem 1.5rem; font-size: 0.95rem; font-weight: 600;
  cursor: pointer; font-family: inherit; transition: opacity 0.15s;
  margin-top: 0.5rem;
}
.btn-primary:hover:not(:disabled) { opacity: 0.85; }
.btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }

.alert { padding: 0.75rem 1rem; border-radius: 8px; font-size: 0.88rem; }
.alert--error { background: rgba(220,38,38,0.1); color: #dc2626; }
.alert--uspeh { background: rgba(34,197,94,0.1); color: #16a34a; }
</style>