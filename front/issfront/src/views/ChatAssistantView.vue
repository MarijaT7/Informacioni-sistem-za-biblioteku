<template>
  <div class="app-layout">
    <SidebarNav />

    <main class="main-content chat-page">
      <p v-if="!authorized" class="error-msg">Nemate pristup ovoj stranici.</p>

      <template v-else>
        <p v-if="healthChecked && !healthOk" class="health-banner">
          AI asistent trenutno nije dostupan. Pokušajte kasnije ili obavestite bibliotekara.
        </p>

        <div class="chat-shell">
          <!-- LEVA KOLONA: dugme + lista sesija -->
          <aside class="session-panel">
            <button class="btn-new-chat" @click="openCompose">Novi čet</button>

            <p v-if="loadingSessions" class="loading-msg">Učitavanje četova...</p>
            <p v-else-if="sessionsError" class="error-msg">{{ sessionsError }}</p>

            <nav v-else class="session-list">
              <p v-if="!sessions.length" class="empty-state">Još nemate nijedan čet.</p>

              <div
                v-for="s in sessions"
                :key="s.id"
                class="session-item"
                :class="{ active: s.id === activeSessionId && viewMode === 'conversation' }"
                @click="selectSession(s.id)"
              >
                <span class="session-name" :class="{ 'session-name-pending': s._pending }">
                  {{ sessionLabel(s) }}
                </span>
                <div class="session-right">
                  <span class="agent-pill" :class="agentPillClass(s.tipAgentaCS)">
                    {{ agentLabel(s.tipAgentaCS) }}
                  </span>
                  <button
                    class="btn-delete-session"
                    :disabled="s._pending || deletingSessionId === s.id"
                    @click.stop="deleteSession(s)"
                    aria-label="Obriši čet"
                    title="Obriši čet"
                  >
                    {{ deletingSessionId === s.id ? '…' : '✕' }}
                  </button>
                </div>
              </div>
            </nav>
          </aside>

          <!-- GRANIČNIK IZMEĐU KOLONA -->
          <div class="column-gutter" aria-hidden="true"></div>

          <!-- DESNA KOLONA: aktivni čet ili compose -->
          <section class="conversation-panel">
            <!-- Stanje: kucanje nove poruke / odabir asistenta -->
            <template v-if="viewMode === 'compose'">
              <div class="compose-state">
                <div class="compose-box">
                  <h1 class="compose-title">Novi čet</h1>
                  <p class="compose-subtitle">Izaberite asistenta i započnite razgovor na engleskom jeziku.</p>

                  <p v-if="newSessionError" class="error-msg">{{ newSessionError }}</p>

                  <form class="compose-form" @submit.prevent="createSession">
                    <textarea
                      v-model="newSessionForm.sadrzajPoruke"
                      class="compose-textarea"
                      rows="3"
                      placeholder="Poruka sa pitanjem"
                      :disabled="creatingSession"
                    ></textarea>

                    <div class="compose-actions">
                      <select
                        v-model="newSessionForm.tipAgentaCS"
                        class="compose-agent-select"
                        :disabled="creatingSession"
                      >
                        <option value="AGENT_KNJIGE">Asistent za knjige</option>
                        <option value="AGENT_RECENZIJE">Asistent za recenzije</option>
                      </select>

                      <button
                        type="submit"
                        class="btn-send"
                        :disabled="creatingSession || !newSessionForm.sadrzajPoruke.trim() || healthChecked && !healthOk" 
                        aria-label="Pošalji"
                      >
                        {{ creatingSession ? '…' : '➤' }}
                      </button>
                    </div>
                  </form>
                </div>
              </div>
            </template>

            <!-- Stanje: aktivan postojeći (ili tek pokrenut) čet -->
            <template v-else-if="viewMode === 'conversation' && activeSession">
              <header class="conversation-header">
                <h1 class="conversation-title">{{ sessionLabel(activeSession) }}</h1>
                <span class="agent-pill" :class="agentPillClass(activeSession.tipAgentaCS)">
                  {{ agentLabel(activeSession.tipAgentaCS) }}
                </span>
              </header>

              <p v-if="loadingMessages" class="loading-msg">Učitavanje poruka...</p>
              <p v-if="messagesError" class="error-msg">{{ messagesError }}</p>

              <div ref="messagesEl" class="messages-area">
                <div
                  v-for="m in messages"
                  :key="m.id"
                  class="msg-row"
                  :class="m.tipCP === 'CLAN' ? 'user-row' : 'agent-row'"
                >
                  <div class="msg-bubble" :class="m.tipCP === 'CLAN' ? 'user-bubble' : 'agent-bubble'">
                    <p v-if="m._pending" class="agent-pending">Agent odgovara...</p>
                    <p v-else class="msg-text">{{ m.sadrzajCP }}</p>

                    <div v-if="m.tipCP === 'AI_ASISTENT' && !m._pending" class="rating-row">
                      <button class="btn-rate" :class="{ rated: m._ocena != null }" @click="openRatingPopup(m)">
                        {{ m._ocena != null ? `Ocenjeno ★${m._ocena}` : 'Oceni' }}
                      </button>
                    </div>
                  </div>
                </div>
              </div>

              <form class="message-form" @submit.prevent="sendMessage">
                <input
                  v-model="draft"
                  type="text"
                  class="message-input"
                  placeholder="Poruka sa pitanjem"
                  :disabled="sending"
                />
                <button type="submit" class="btn-send" :disabled="sending || !draft.trim()" aria-label="Pošalji">
                  ➤
                </button>
              </form>
            </template>

            <!-- Stanje: ništa odabrano -->
            <div v-else class="no-session-state">
              <p>Izaberite postojeći čet sa leve strane ili pokrenite novi.</p>
            </div>
          </section>
        </div>
      </template>
    </main>

    <!-- POPUP: ocenjivanje poruke -->
    <Teleport to="body">
      <Transition name="modal-pop">
        <div v-if="ratingPopup.open" class="modal-overlay" @click.self="closeRatingPopup">
          <div class="modal-box rating-modal">
            <h2>Ocenite odgovor</h2>
          
            <div class="form-group">
              <label>Broj zvezdica</label>
              <div class="star-picker">
                <button
                  v-for="star in 5"
                  :key="star"
                  type="button"
                  class="star-btn"
                  :class="{ filled: star <= ratingPopup.ocena }"
                  @click="ratingPopup.ocena = star"
                >★</button>
              </div>
            </div>
          
            <div class="form-group">
              <label for="rating-comment">Komentar</label>
              <textarea
                id="rating-comment"
                v-model="ratingPopup.komentar"
                class="form-textarea"
                rows="3"
                placeholder="Opišite zašto ste dali ovu ocenu (opciono)..."
              ></textarea>
            </div>
          
            <p v-if="ratingPopup.error" class="error-msg">{{ ratingPopup.error }}</p>
          
            <div class="modal-actions">
              <button class="btn-secondary2" type="button" @click="closeRatingPopup" :disabled="ratingPopup.saving">
                Otkažite
              </button>
              <button
                class="btn-primary"
                type="button"
                @click="submitRating"
                :disabled="ratingPopup.saving || !ratingPopup.ocena"
              >
                {{ ratingPopup.saving ? 'Čuvam...' : 'Sačuvajte ocenu' }}
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
import SidebarNav from '../components/Sidebar.vue'
import { useAuthStore } from '../stroage/auth.js'
import { cetSesijaApi, cetPorukaApi, chatHealthApi } from '../services/api.js'

const authStore = useAuthStore()

const authorized = ref(false)

const healthChecked = ref(false)
const healthOk = ref(true)

const sessions = ref([])
const loadingSessions = ref(false)
const sessionsError = ref('')

// 'compose' | 'conversation'
const viewMode = ref('compose')

const activeSessionId = ref(null)
const messages = ref([])
const loadingMessages = ref(false)
const messagesError = ref('')
const messagesEl = ref(null)

const draft = ref('')
const sending = ref(false)

const creatingSession = ref(false)
const deletingSessionId = ref(null)
const newSessionError = ref('')
const newSessionForm = ref({
  tipAgentaCS: 'AGENT_KNJIGE',
  sadrzajPoruke: ''
})

const ratingPopup = ref({
  open: false,
  message: null,
  ocena: 0,
  komentar: '',
  saving: false,
  error: ''
})

const AGENT_LABELS = {
  AGENT_KNJIGE: 'Knjige',
  AGENT_RECENZIJE: 'Recenzije'
}
const agentLabel = (tip) => AGENT_LABELS[tip] || tip || '—'
const agentPillClass = (tip) =>
  tip === 'AGENT_RECENZIJE' ? 'pill-recenzije' : 'pill-knjige'

const activeSession = computed(() =>
  sessions.value.find((s) => s.id === activeSessionId.value) || null
)

function sessionLabel(session) {
  return session?.naslovCS || 'Bez naziva'
}

onMounted(async () => {
  const role = authStore.getRole()
  authorized.value = role === 'CLAN'
  if (!authorized.value) return

  await checkHealth()
  await loadSessions()
})

async function checkHealth() {
  try {
    const res = await chatHealthApi.provera()
    healthOk.value = !!res.data?.ollama_available
  } catch {
    healthOk.value = false
  } finally {
    healthChecked.value = true
  }
}

async function loadSessions() {
  loadingSessions.value = true
  sessionsError.value = ''
  try {
    const res = await cetSesijaApi.sve()
    sessions.value = res.data || []
  } catch (e) {
    sessionsError.value = e.response?.status === 403
      ? 'Sesija je istekla. Prijavite se ponovo.'
      : 'Greška pri učitavanju četova.'
    sessions.value = []
  } finally {
    loadingSessions.value = false
  }
}

async function selectSession(id) {
  if (id === activeSessionId.value && viewMode.value === 'conversation') return
  activeSessionId.value = id
  viewMode.value = 'conversation'
  await loadMessages(id)
}

async function loadMessages(id) {
  loadingMessages.value = true
  messagesError.value = ''
  messages.value = []
  try {
    const res = await cetSesijaApi.jedna(id)
    messages.value = (res.data?.poruke || []).map((p) => ({ ...p }))
    await scrollToBottom()
    await loadRatings()
  } catch (e) {
    if (e.response?.status === 404) {
      messagesError.value = 'Ovaj čet ne postoji ili je obrisan.'
    } else {
      messagesError.value = 'Greška pri učitavanju poruka.'
    }
  } finally {
    loadingMessages.value = false
  }
}

async function loadRatings() {
  const agentMessages = messages.value.filter((m) => m.tipCP === 'AI_ASISTENT')
  await Promise.all(
    agentMessages.map(async (m) => {
      try {
        const res = await cetPorukaApi.ocena(m.id)
        m._ocena = res.data?.ocenaCP ?? null
        m._komentar = res.data?.komentarCP ?? ''
      } catch {
        m._ocena = null
        m._komentar = ''
      }
    })
  )
}

function openRatingPopup(message) {
  ratingPopup.value = {
    open: true,
    message,
    ocena: message._ocena ?? 0,
    komentar: message._komentar ?? '',
    saving: false,
    error: ''
  }
}

function closeRatingPopup() {
  if (ratingPopup.value.saving) return
  ratingPopup.value.open = false
}

async function submitRating() {
  const popup = ratingPopup.value
  if (!popup.ocena || !popup.message) return

  popup.saving = true
  popup.error = ''
  try {
    const res = await cetPorukaApi.oceni(popup.message.id, popup.ocena, popup.komentar.trim())
    popup.message._ocena = res.data?.ocenaCP ?? popup.ocena
    popup.message._komentar = res.data?.komentarCP ?? popup.komentar.trim()
    popup.open = false
  } catch {
    popup.error = 'Greška pri čuvanju ocene. Pokušajte ponovo.'
  } finally {
    popup.saving = false
  }
}

async function sendMessage() {
  const sadrzaj = draft.value.trim()
  if (!sadrzaj || !activeSessionId.value) return

  sending.value = true
  draft.value = ''

  const userMsg = {
    id: `temp-user-${Date.now()}`,
    tipCP: 'CLAN',
    sadrzajCP: sadrzaj
  }
  const pendingAgentMsg = {
    id: `temp-agent-${Date.now()}`,
    tipCP: 'AI_ASISTENT',
    sadrzajCP: '',
    _pending: true
  }
  messages.value.push(userMsg, pendingAgentMsg)
  await scrollToBottom()

  try {
    const res = await cetPorukaApi.nova(activeSessionId.value, sadrzaj)
    const data = res.data || {}
    userMsg.id = data.porukaClana?.id ?? userMsg.id
    pendingAgentMsg.id = data.porukaAgenta?.id ?? pendingAgentMsg.id
    pendingAgentMsg.sadrzajCP = data.porukaAgenta?.sadrzajCP ?? ''
    pendingAgentMsg._pending = false
    pendingAgentMsg._ocena = null
    pendingAgentMsg._komentar = ''
  } catch (e) {
    pendingAgentMsg._pending = false
    pendingAgentMsg.sadrzajCP = e.response?.status === 404
      ? 'Ovaj čet više ne postoji.'
      : 'Greška pri dobijanju odgovora. Pokušajte ponovo.'
  } finally {
    sending.value = false
    await scrollToBottom()
  }
}

async function scrollToBottom() {
  await nextTick()
  if (messagesEl.value) {
    messagesEl.value.scrollTop = messagesEl.value.scrollHeight
  }
}

function openCompose() {
  newSessionForm.value = { tipAgentaCS: 'AGENT_KNJIGE', sadrzajPoruke: '' }
  newSessionError.value = ''
  viewMode.value = 'compose'
}

async function createSession() {
  newSessionError.value = ''
  const sadrzaj = newSessionForm.value.sadrzajPoruke.trim()
  if (!sadrzaj) {
    newSessionError.value = 'Unesite poruku da biste započeli čet.'
    return
  }

  creatingSession.value = true

  // Optimistički ubacujemo privremenu sesiju u levu listu da korisnik
  // odmah vidi da je čet pokrenut, dok čekamo prawi odgovor sa servera.
  const tempId = `temp-session-${Date.now()}`
  const tipAgenta = newSessionForm.value.tipAgentaCS
  const tempSession = {
    id: tempId,
    naslovCS: 'Novo ćaskanje...',
    tipAgentaCS: tipAgenta,
    _pending: true
  }
  sessions.value = [tempSession, ...sessions.value]
  activeSessionId.value = tempId
  viewMode.value = 'conversation'
  messages.value = [
    { id: `temp-user-${Date.now()}`, tipCP: 'CLAN', sadrzajCP: sadrzaj },
    { id: `temp-agent-${Date.now()}`, tipCP: 'AI_ASISTENT', sadrzajCP: '', _pending: true }
  ]
  await scrollToBottom()

  try {
    const res = await cetSesijaApi.nova(tipAgenta, sadrzaj)
    const newSession = res.data

    // Zamenjujemo privremenu sesiju pravom (server vraća naslovCS
    // već generisan u istom odgovoru, pa nema potrebe za dodatnim fetch-om).
    const idx = sessions.value.findIndex((s) => s.id === tempId)
    if (idx !== -1 && newSession?.id) {
      sessions.value.splice(idx, 1, newSession)
    } else {
      await loadSessions()
    }

    if (newSession?.id) {
      activeSessionId.value = newSession.id
      messages.value = (newSession.poruke || []).map((p) => ({ ...p, _ocena: null, _komentar: '' }))
      await scrollToBottom()
    }
  } catch (e) {
    // Uklanjamo privremenu sesiju jer kreiranje nije uspelo, i vraćamo
    // korisnika na compose ekran sa porukom o grešci da ne izgubi tekst.
    sessions.value = sessions.value.filter((s) => s.id !== tempId)
    activeSessionId.value = null
    viewMode.value = 'compose'

    if (e.response?.status === 400) {
      newSessionError.value = 'Nedostaje sadržaj poruke ili tip asistenta.'
    } else if (e.response?.status === 403) {
      newSessionError.value = 'Nemate dozvolu za ovu akciju. Prijavite se ponovo.'
    } else {
      newSessionError.value = 'Greška pri pokretanju novog četa.'
    }
  } finally {
    creatingSession.value = false
  }
}

async function deleteSession(session) {
  if (session._pending) return
  if (!confirm(`Obrisati čet "${sessionLabel(session)}"?`)) return

  deletingSessionId.value = session.id
  try {
    await cetSesijaApi.obrisi(session.id)
    sessions.value = sessions.value.filter((s) => s.id !== session.id)

    // Ako je obrisana aktivna sesija, vrati na compose ekran
    if (activeSessionId.value === session.id) {
      activeSessionId.value = null
      viewMode.value = 'compose'
      messages.value = []
    }
  } catch (e) {
    sessionsError.value = e.response?.status === 404
      ? 'Čet sesija nije pronađena.'
      : 'Greška pri brisanju. Pokušajte ponovo.'
  } finally {
    deletingSessionId.value = null
  }
}
</script>

<style scoped>
.chat-page { display: flex; flex-direction: column; flex: 1; min-height: 0; }

.health-banner {
  background: #fbe6c9;
  color: #7a4f12;
  border-radius: 8px;
  padding: 0.6rem 1rem;
  margin-bottom: 1rem;
  font-size: 0.9rem;
}

.chat-shell {
  display: grid;
  grid-template-columns: 260px 1px 1fr;
  flex: 1;
  min-height: 0;
}

/* ── Graničnik između kolona ─────────────── */
.column-gutter {
  background: #ffffff;
  width: 1px;
  margin: 0 1.5rem;
}

/* ── Leva kolona ──────────────────────────── */
.session-panel {
  display: flex;
  flex-direction: column;
  min-height: 0;
  padding-right: 0.25rem;
}

.btn-new-chat {
  background: var(--btn-primary);
  color: var(--text-light);
  border: none;
  border-radius: 999px;
  padding: 0.7rem 1rem;
  font-weight: 700;
  font-size: 0.95rem;
  cursor: pointer;
  transition: background 0.2s;
  flex-shrink: 0;
}
.btn-new-chat:hover { background: var(--btn-hover); }

.session-list {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
  margin-top: 0.9rem;
  overflow-y: auto;
  min-height: 0;
  padding-right: 0.2rem;
}

.session-item {
  background: var(--input-bg);
  border: 2px solid var(--border);
  text-align: left;
  padding: calc(0.75rem - 0.5px) calc(0.9rem - 0.5px);
  font-size: 1rem;
  font-weight: 700;
  color: var(--text-dark);
  cursor: pointer;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.9rem;
  transition: border-color 0.15s;
}
.session-item:hover  { border-color: var(--btn-primary); }
.session-item.active { border-color: var(--btn-primary); }

.session-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  letter-spacing: 0.2px;
}
.session-name-pending { font-style: italic; opacity: 0.75; }

.agent-pill {
  font-size: 0.7rem;
  font-weight: 700;
  letter-spacing: 0.3px;
  padding: 0.25rem 0.7rem;
  border-radius: 999px;
  flex-shrink: 0;
}
.pill-knjige     { background: #dde8d8; color: #2f5024; }
.pill-recenzije  { background: #dbe6f3; color: #1e4670; }

/* ── Desna kolona ─────────────────────────── */
.conversation-panel {
  display: flex;
  flex-direction: column;
  min-height: 0;
  padding-left: 3.5rem;
}

.conversation-header {
  display: flex;
  align-items: center;
  gap: 0.9rem;
  margin-bottom: 1.2rem;
  flex-shrink: 0;
}
.conversation-title { font-size: 1.8rem; margin: 0; }

.messages-area {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 0.9rem;
  padding-right: 0.3rem;
  margin-bottom: 1rem;
  min-height: 0;
}

.msg-row { display: flex; }
.user-row  { justify-content: flex-end; }
.agent-row { justify-content: flex-start; }

.msg-bubble {
  max-width: 70%;
  border-radius: 14px;
  padding: 0.7rem 1.1rem;
  font-size: 0.95rem;
  line-height: 1.45;
  box-shadow: var(--shadow);
}
.user-bubble  { background: white; border: 1px solid var(--border); color: var(--text-dark); }
.agent-bubble { background: white; border: 1px solid var(--border); color: var(--text-dark); }
.msg-text { margin: 0; white-space: pre-wrap; }

.rating-row {
  display: flex;
  align-items: center;
  margin-top: 0.5rem;
  padding-top: 0.4rem;
  border-top: 1px solid var(--border);
}

.btn-rate {
  background: transparent;
  border: 1.5px solid var(--border);
  border-radius: 999px;
  padding: 0.3rem 0.9rem;
  font-size: 0.78rem;
  font-weight: 600;
  color: var(--text-mid);
  cursor: pointer;
  transition: border-color 0.15s, color 0.15s, background 0.15s;
}
.btn-rate:hover { border-color: var(--btn-primary); color: var(--text-dark); }
.btn-rate.rated {
  background: #fbf1dd;
  border-color: #d9a443;
  color: #7a5c1d;
}

.message-form {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  background: var(--input-bg);
  border: 1.5px solid var(--border);
  border-radius: 999px;
  padding: 0.5rem 0.5rem 0.5rem 1.2rem;
  flex-shrink: 0;
}
.message-input {
  flex: 1;
  border: none;
  background: transparent;
  font-size: 0.95rem;
  outline: none;
  color: var(--text-dark);
}
.btn-send {
  background: var(--btn-primary, #7a5c48);
  color: white;
  border: none;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.btn-send:hover    { opacity: 0.9; }
.btn-send:disabled { opacity: 0.5; cursor: not-allowed; }

.no-session-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-mid);
  text-align: center;
}

/* ── Compose stanje (zamena za stari modal) ── */
.compose-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.compose-box {
  width: 100%;
  max-width: 540px;
  text-align: center;
}

.compose-title { font-size: 1.9rem; margin-bottom: 0.4rem; }
.compose-subtitle { color: var(--text-mid); margin-bottom: 1.4rem; }

.compose-form {
  background: var(--input-bg);
  border: 1.5px solid var(--border);
  border-radius: 18px;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  text-align: left;
}

.compose-textarea {
  border: none;
  background: transparent;
  font-size: 1rem;
  color: var(--text-dark);
  resize: vertical;
  outline: none;
  font-family: inherit;
}

.compose-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
}

.compose-agent-select {
  background: var(--card-bg-alt);
  border: 1.5px solid var(--border);
  border-radius: 999px;
  padding: 0.5rem 0.9rem;
  font-size: 0.9rem;
  color: var(--text-dark);
}

/* ── Popup za ocenjivanje ─────────────────── */
.rating-modal { text-align: left; }
.rating-modal h2 { text-align: center; margin-bottom: 1.2rem; }

.star-picker { display: flex; gap: 0.25rem; }
.star-btn {
  background: transparent;
  border: none;
  font-size: 1.6rem;
  cursor: pointer;
  color: #c9c2bb;
  padding: 0.1rem;
  line-height: 1;
}
.star-btn.filled { color: #d9a443; }

.form-group { margin-top: 1rem; display: flex; flex-direction: column; gap: 0.4rem; }
.form-group label { font-size: 0.85rem; font-weight: 600; color: var(--text-mid); }

.form-textarea {
  background: var(--input-bg);
  border: 1.5px solid var(--border);
  border-radius: 8px;
  padding: 0.6rem 0.75rem;
  font-size: 0.95rem;
  color: var(--text-dark);
  resize: vertical;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  margin-top: 1.4rem;
}

@media (max-width: 768px) {
  .chat-shell { grid-template-columns: 1fr; }
  .column-gutter { display: none; }
  .session-panel { order: 2; }
  .conversation-panel { order: 1; }
}


.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-box {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.25);
  padding: 24px;
  max-width: 420px;
  width: 90%;
}

.modal-pop-enter-active,
.modal-pop-leave-active {
  transition: opacity 0.18s ease;
}
.modal-pop-enter-active .modal-box,
.modal-pop-leave-active .modal-box {
  transition: transform 0.18s ease, opacity 0.18s ease;
}
.modal-pop-enter-from,
.modal-pop-leave-to {
  opacity: 0;
}
.modal-pop-enter-from .modal-box,
.modal-pop-leave-to .modal-box {
  transform: scale(0.92) translateY(8px);
  opacity: 0;
}

.btn-secondary2 {
  background: var(--radio-inactive);
  color: var(--text-dark);
  border: none;
  border-radius: 50px;
  padding: 0.65rem 1.8rem;
  font-size: 1.15rem;
  cursor: pointer;
  transition: background 0.2s;
  margin: 2rem auto 0;
  font-weight: bold;
}
.btn-secondary2:hover { background: #8fa870; }

.session-right {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-shrink: 0;
}

.btn-delete-session {
  background: transparent;
  border: none;
  color: var(--text-mid);
  cursor: pointer;
  padding: 0.2rem 0.45rem;
  border-radius: 4px;
  font-size: 0.82rem;
  font-weight: 700;
  transition: opacity 0.15s, color 0.15s, background 0.15s;
  color: #c0392b;
}
.session-item:hover .btn-delete-session { opacity: 1; }
.btn-delete-session:hover { color: #c0392b; background: #fdecea; }
.btn-delete-session:disabled { opacity: 0.4; cursor: not-allowed; }
</style>