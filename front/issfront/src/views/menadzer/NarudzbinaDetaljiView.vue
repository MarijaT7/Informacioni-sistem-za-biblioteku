<template>
  <div class="detalji-wrapper">
    <div class="page-header">
      <div>
        <h1>Narudžbine</h1>
        <p class="subtitle">Detalji narudžbine</p>
      </div>
      <button class="btn-sekundarni" @click="router.push('/menadzer/narudzbine')">← Nazad</button>
    </div>

    <div v-if="loading" class="state-msg">Učitavanje...</div>
    <div v-else-if="error" class="state-msg state-msg--error">{{ error }}</div>

    <div v-else>
      <!-- Info kartica -->
      <div class="kartica">
        <div class="kartica-header">
          <div>
            <h2>Narudžbina #{{ narudzbina.id }}</h2>
            <span class="status-badge" :class="statusKlasa(narudzbina.status)">
              {{ statusNaziv(narudzbina.status) }}
            </span>
          </div>
          <div class="header-akcije">
            <button
              v-if="narudzbina.status === 'KREIRANA'"
              class="btn-akcija btn-potvrdi"
              @click="potvrdi"
              :disabled="!narudzbina.stavke || narudzbina.stavke.length === 0">
              ✓ Potvrdi narudžbinu
            </button>
            <button
              v-if="narudzbina.status === 'KREIRANA'"
              class="btn-akcija btn-otkazi"
              @click="showOtkaziModal = true">
              Otkaži
            </button>
            <button
              v-if="narudzbina.status === 'KREIRANA'"
              class="btn-akcija btn-isporuka"
              @click="showIsporukaModal = true">
              Evidentiraj isporuku
            </button>
            <button
              v-if="narudzbina.status === 'ISPORUCENA'"
              class="btn-akcija btn-reklamacija"
              @click="showReklamacijaModal = true">
              + Reklamacija
            </button>
          </div>
        </div>

        <div class="info-grid">
          <div class="info-row">
            <span class="info-label">Dobavljač</span>
            <span class="info-value">{{ narudzbina.dobavljacNaziv }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">Popust</span>
            <span class="info-value">{{ narudzbina.popust }}%</span>
          </div>
          <div class="info-row">
            <span class="info-label">Datum kreiranja</span>
            <span class="info-value">{{ narudzbina.datumKreiranja }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">Očekivana isporuka</span>
            <span class="info-value">{{ narudzbina.datumOcekivaneIsporuke }}</span>
          </div>
          <div class="info-row" v-if="narudzbina.datumStvarneIsporuke">
            <span class="info-label">Stvarna isporuka</span>
            <span class="info-value">{{ narudzbina.datumStvarneIsporuke }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">Ukupna cena</span>
            <span class="info-value info-value--accent">{{ formatirajIznos(narudzbina.ukupnaCena) }}</span>
          </div>
          <div class="info-row" v-if="narudzbina.napomena">
            <span class="info-label">Napomena</span>
            <span class="info-value">{{ narudzbina.napomena }}</span>
          </div>
        </div>
      </div>

      <!-- Stavke -->
      <div class="sekcija-header">
        <h2>Stavke narudžbine</h2>
        <button
          v-if="narudzbina.status === 'KREIRANA'"
          class="btn-primary"
          @click="showDodajStavkuModal = true">
          + Dodaj stavku
        </button>
      </div>

      <div v-if="!narudzbina.stavke || narudzbina.stavke.length === 0" class="state-msg">
        Nema stavki. Dodajte knjige u narudžbinu.
      </div>

      <div v-else class="table-wrapper">
        <table class="tabla">
          <thead>
            <tr>
              <th>Knjiga</th>
              <th>Autor</th>
              <th>Količina</th>
              <th>Cena/kom</th>
              <th>Ukupno</th>
              <th v-if="narudzbina.status === 'KREIRANA'">Akcije</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="s in narudzbina.stavke" :key="s.id">
              <td class="td-bold">{{ s.naslov }}</td>
              <td>{{ s.autor }}</td>
              <td>{{ s.kolicina }}</td>
              <td>{{ formatirajIznos(s.cenaPoKomadu) }}</td>
              <td>{{ formatirajIznos(s.ukupnaCenaStavke) }}</td>
              <td v-if="narudzbina.status === 'KREIRANA'">
                <button class="btn-akcija btn-otkazi" @click="ukloniStavku(s.id)">
                  Ukloni
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal dodaj stavku -->
    <div v-if="showDodajStavkuModal" class="modal-overlay" @click.self="showDodajStavkuModal = false">
      <div class="modal">
        <h2>Dodaj stavku</h2>
        <div v-if="stavkaError" class="alert alert--error">{{ stavkaError }}</div>

        <div class="form-group">
          <label>Knjiga (ISBN)</label>
          <select v-model="stavkaForma.isbn">
            <option :value="null" disabled>Izaberite knjigu</option>
            <option v-for="k in knjige":key="k.predlogId ?? k.isbn"
                                       :value="k">
              {{ k.naslov }} — {{ k.autor }}
            </option>
          </select>
        </div>
        <div class="form-group">
          <label>Količina</label>
          <input v-model="stavkaForma.kolicina" type="number" min="1" placeholder="npr. 5" />
        </div>
        <div class="form-group">
          <label>Okvirna cena po komadu (RSD)</label>
          <input v-model="stavkaForma.okvirnaCena" type="number" min="0.01" step="0.01" placeholder="npr. 1500" />
        </div>
        <div class="modal-akcije">
          <button class="btn-primary" @click="dodajStavku" :disabled="loadingStavka">
            {{ loadingStavka ? 'Dodavanje...' : 'Dodaj' }}
          </button>
          <button class="btn-sekundarni" @click="showDodajStavkuModal = false">Odustani</button>
        </div>
      </div>
    </div>

    <!-- Modal isporuka -->
    <div v-if="showIsporukaModal" class="modal-overlay" @click.self="showIsporukaModal = false">
      <div class="modal">
        <h2>Evidentiranje isporuke</h2>
        <div v-if="isporukaError" class="alert alert--error">{{ isporukaError }}</div>
        <div class="form-group">
          <label>Datum stvarne isporuke</label>
          <input v-model="datumIsporuke" type="date" />
        </div>
        <div class="modal-akcije">
          <button class="btn-primary" @click="evidentirajIsporuku">Potvrdi</button>
          <button class="btn-sekundarni" @click="showIsporukaModal = false">Odustani</button>
        </div>
      </div>
    </div>

    <!-- Modal reklamacija -->
    <div v-if="showReklamacijaModal" class="modal-overlay" @click.self="showReklamacijaModal = false">
      <div class="modal">
        <h2>Kreiranje reklamacije</h2>
        <div v-if="reklamacijaError" class="alert alert--error">{{ reklamacijaError }}</div>
        <div class="form-group">
          <label>Razlog reklamacije</label>
          <textarea v-model="reklamacijaForma.razlog" rows="3" placeholder="Opišite problem..."></textarea>
        </div>
        <div class="form-group">
          <label>Napomena (opciono)</label>
          <textarea v-model="reklamacijaForma.napomena" rows="2" placeholder="Dodatne napomene..."></textarea>
        </div>
        <div class="modal-akcije">
          <button class="btn-primary" @click="kreirajReklamaciju">Kreiraj reklamaciju</button>
          <button class="btn-sekundarni" @click="showReklamacijaModal = false">Odustani</button>
        </div>
      </div>
    </div>

    <!-- Modal otkaži -->
    <div v-if="showOtkaziModal" class="modal-overlay" @click.self="showOtkaziModal = false">
      <div class="modal">
        <h2>Otkazivanje narudžbine</h2>
        <p>Da li ste sigurni da želite da otkažete narudžbinu #{{ narudzbina.id }}? Rezervisana sredstva će biti oslobođena.</p>
        <div class="modal-akcije">
          <button class="btn-otkazi-modal" @click="otkaziNarudzbinu">Da, otkaži</button>
          <button class="btn-sekundarni" @click="showOtkaziModal = false">Ne</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { narudzbinApi, reklamacijaApi, knjigaApi, predlogApi, sistemskePreporukeApi } from '../../services/api.js'

const router = useRouter()
const route = useRoute()
const id = route.params.id

const narudzbina = ref({})
const knjige = ref([])
const loading = ref(false)
const error = ref('')

const showDodajStavkuModal = ref(false)
const showIsporukaModal = ref(false)
const showReklamacijaModal = ref(false)
const showOtkaziModal = ref(false)

const stavkaForma = ref({ isbn: null, kolicina: '', okvirnaCena: '' })
const stavkaError = ref('')
const loadingStavka = ref(false)

const datumIsporuke = ref('')
const isporukaError = ref('')

const reklamacijaForma = ref({ razlog: '', napomena: '' })
const reklamacijaError = ref('')

async function ucitaj() {
  loading.value = true
  error.value = ''
  try {
    const res = await narudzbinApi.getJedna(id)
    narudzbina.value = res.data
  } catch (e) {
    error.value = e.response?.data?.message || 'Greška pri učitavanju narudžbine.'
  } finally {
    loading.value = false
  }
}

async function ucitajKnjige() {
    
    try {

    const [predloziRes, preporukeRes] = await Promise.all([
    predlogApi.zaNarudzbinu(),
    sistemskePreporukeApi.zaNarudzbinu()
    ])

    knjige.value = [
    ...predloziRes.data,
    ...preporukeRes.data
]
  } catch (e) {
    console.log('Greška pri učitavanju knjiga:', e)
  }
}

async function dodajStavku() {
  stavkaError.value = ''
  if (!stavkaForma.value.isbn || !stavkaForma.value.kolicina || !stavkaForma.value.okvirnaCena) {
    stavkaError.value = 'Sva polja su obavezna.'
    return
  }
  loadingStavka.value = true
  try {
    const res = await narudzbinApi.dodajStavku(id, {
      isbn: stavkaForma.value.isbn,
      kolicina: Number(stavkaForma.value.kolicina),
      okvirnaCena: Number(stavkaForma.value.okvirnaCena)
    })
    narudzbina.value = res.data
    showDodajStavkuModal.value = false
    stavkaForma.value = { isbn: null, kolicina: '', okvirnaCena: '' }
  } catch (e) {
    stavkaError.value = e.response?.data?.message || 'Greška pri dodavanju stavke.'
  } finally {
    loadingStavka.value = false
  }
}

async function ukloniStavku(stavkaId) {
  try {
    const res = await narudzbinApi.ukloniStavku(id, stavkaId)
    narudzbina.value = res.data
  } catch (e) {
    error.value = e.response?.data?.message || 'Greška pri uklanjanju stavke.'
  }
}

async function potvrdi() {
  try {
    const res = await narudzbinApi.potvrdi(id)
    narudzbina.value = res.data
  } catch (e) {
    error.value = e.response?.data?.message || 'Greška pri potvrđivanju narudžbine.'
  }
}

async function evidentirajIsporuku() {
  isporukaError.value = ''
  if (!datumIsporuke.value) {
    isporukaError.value = 'Datum isporuke je obavezan.'
    return
  }
  try {
    const res = await narudzbinApi.evidentirajIsporuku(id, {
      datumStvarneIsporuke: datumIsporuke.value
    })
    narudzbina.value = res.data
    showIsporukaModal.value = false
  } catch (e) {
    isporukaError.value = e.response?.data?.message || 'Greška pri evidentiranju isporuke.'
  }
}

async function kreirajReklamaciju() {
  reklamacijaError.value = ''
  if (!reklamacijaForma.value.razlog.trim()) {
    reklamacijaError.value = 'Razlog je obavezan.'
    return
  }
  try {
    await reklamacijaApi.kreiraj(id, reklamacijaForma.value)
    await ucitaj()
    showReklamacijaModal.value = false
    reklamacijaForma.value = { razlog: '', napomena: '' }
  } catch (e) {
    reklamacijaError.value = e.response?.data?.message || 'Greška pri kreiranju reklamacije.'
  }
}

async function otkaziNarudzbinu() {
  try {
    await narudzbinApi.otkazi(id)
    router.push('/menadzer/narudzbine')
  } catch (e) {
    error.value = e.response?.data?.message || 'Greška pri otkazivanju narudžbine.'
    showOtkaziModal.value = false
  }
}

function statusKlasa(status) {
  if (status === 'KREIRANA') return 'status--kreirana'
  if (status === 'ISPORUCENA') return 'status--isporucena'
  if (status === 'REKLAMIRANA') return 'status--reklamirana'
  if (status === 'OTKAZANA') return 'status--otkazana'
  return ''
}

function statusNaziv(status) {
  if (status === 'KREIRANA') return 'Kreirana'
  if (status === 'ISPORUCENA') return 'Isporučena'
  if (status === 'REKLAMIRANA') return 'Reklamirana'
  if (status === 'OTKAZANA') return 'Otkazana'
  return status
}

function formatirajIznos(iznos) {
  if (!iznos) return '0,00 RSD'
  return Number(iznos).toLocaleString('sr-RS', { minimumFractionDigits: 2 }) + ' RSD'
}

onMounted(() => {
  ucitaj()
  ucitajKnjige()
})
</script>

<style scoped>
.detalji-wrapper { max-width: 900px; }

.page-header {
  display: flex; justify-content: space-between;
  align-items: flex-start; margin-bottom: 2rem;
}
.page-header h1 { margin: 0 0 0.25rem; font-size: 2rem; color: var(--text-h); }
.subtitle { color: var(--text); font-size: 0.95rem; margin: 0; }

.kartica { border: 1px solid var(--border); border-radius: 16px; padding: 1.5rem; margin-bottom: 2rem; }
.kartica-header {
  display: flex; justify-content: space-between; align-items: flex-start;
  margin-bottom: 1.5rem; flex-wrap: wrap; gap: 1rem;
}
.kartica-header h2 { margin: 0 0 0.5rem; color: var(--text-h); }

.header-akcije { display: flex; gap: 0.75rem; flex-wrap: wrap; align-items: center; }

.info-grid { display: flex; flex-direction: column; gap: 0.5rem; }
.info-row { display: flex; gap: 1rem; padding: 0.5rem 0; border-bottom: 1px solid var(--border); }
.info-row:last-child { border-bottom: none; }
.info-label { width: 160px; flex-shrink: 0; font-size: 0.85rem; color: var(--text); font-weight: 500; }
.info-value { color: var(--text-h); }
.info-value--accent { color: var(--accent); font-weight: 700; font-size: 1.05rem; }

.sekcija-header {
  display: flex; justify-content: space-between;
  align-items: center; margin-bottom: 1rem;
}
.sekcija-header h2 { margin: 0; color: var(--text-h); }

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

.btn-primary {
  background: var(--accent); color: #fff; border: none; border-radius: 8px;
  padding: 0.6rem 1.2rem; font-size: 0.9rem; font-weight: 600;
  cursor: pointer; font-family: inherit; transition: opacity 0.15s;
}
.btn-primary:hover:not(:disabled) { opacity: 0.85; }
.btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }

.btn-sekundarni {
  background: transparent; color: var(--text); border: 1px solid var(--border);
  border-radius: 8px; padding: 0.6rem 1.2rem; font-size: 0.9rem;
  font-weight: 500; cursor: pointer; font-family: inherit; transition: background 0.15s;
}
.btn-sekundarni:hover { background: var(--accent-bg); }

.btn-akcija {
  padding: 0.35rem 0.85rem; border-radius: 6px; font-size: 0.82rem;
  font-weight: 500; border: none; cursor: pointer; font-family: inherit; transition: opacity 0.15s;
}
.btn-akcija:hover:not(:disabled) { opacity: 0.8; }
.btn-akcija:disabled { opacity: 0.4; cursor: not-allowed; }
.btn-potvrdi { background: rgba(34,197,94,0.1); color: #16a34a; }
.btn-otkazi { background: rgba(107,114,128,0.1); color: #6b7280; }
.btn-isporuka { background: var(--accent-bg); color: var(--accent); }
.btn-reklamacija { background: rgba(220,38,38,0.1); color: #dc2626; }

.state-msg { text-align: center; padding: 3rem; color: var(--text); }
.state-msg--error { color: #dc2626; }

.modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.4);
  display: flex; align-items: center; justify-content: center; z-index: 100;
}
.modal {
  background: var(--bg); border: 1px solid var(--border); border-radius: 16px;
  padding: 2rem; max-width: 460px; width: 90%;
}
.modal h2 { margin: 0 0 0.75rem; color: var(--text-h); }
.modal p { color: var(--text); margin-bottom: 1.25rem; }

.form-group { display: flex; flex-direction: column; gap: 0.4rem; margin-bottom: 1rem; }
.form-group label { font-size: 0.9rem; color: var(--text); font-weight: 500; }
.form-group input,
.form-group select,
.form-group textarea {
  padding: 0.65rem 1rem; border: 1px solid var(--border); border-radius: 8px;
  font-size: 0.95rem; color: var(--text-h); background: var(--bg);
  font-family: inherit; outline: none; transition: border-color 0.15s;
}
.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus { border-color: var(--accent); }
.form-group textarea { resize: vertical; }

.modal-akcije { display: flex; gap: 0.75rem; margin-top: 1.25rem; }

.btn-otkazi-modal {
  background: rgba(220,38,38,0.1); color: #dc2626; border: none; border-radius: 8px;
  padding: 0.6rem 1.2rem; font-size: 0.9rem; font-weight: 600;
  cursor: pointer; font-family: inherit; transition: opacity 0.15s;
}
.btn-otkazi-modal:hover { opacity: 0.8; }

.alert { padding: 0.75rem 1rem; border-radius: 8px; font-size: 0.9rem; margin-bottom: 0.5rem; }
.alert--error { background: rgba(220,38,38,0.1); color: #dc2626; }
</style>