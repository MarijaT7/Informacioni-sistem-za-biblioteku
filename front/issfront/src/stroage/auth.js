import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const token    = ref(localStorage.getItem('token') || null)
  const user     = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const regJmbg  = ref(localStorage.getItem('regJmbg') || null)  // persists across steps

  const isLoggedIn = computed(() => !!token.value)

  function setAuth(authResponse) {
    token.value = authResponse.token
    user.value  = {
      jmbg:      authResponse.jmbg,
      email:     authResponse.email,
      role:      authResponse.role,
      firstName: authResponse.firstName,
      lastName:  authResponse.lastName,
    }
    localStorage.setItem('token', token.value)
    localStorage.setItem('user',  JSON.stringify(user.value))
  }

  function setRegJmbg(jmbg) {
    regJmbg.value = jmbg
    localStorage.setItem('regJmbg', jmbg)
  }

  function logout() {
    token.value = null
    user.value  = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    localStorage.removeItem('regJmbg')
  }

  return { token, user, regJmbg, isLoggedIn, setAuth, setRegJmbg, logout }
})
