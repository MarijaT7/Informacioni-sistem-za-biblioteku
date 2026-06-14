<template>
  <aside class="sidebar">
    <RouterLink to="/profile" class="sidebar-profile">
      <div class="sidebar-avatar">👤</div>
      <div class="sidebar-name">
        {{ authStore.user?.firstName }}<br />{{ authStore.user?.lastName }}
      </div>
    </RouterLink>
    <nav class="sidebar-nav">
      <RouterLink class="nav-item" to="#">
        <span class="nav-icon"></span> Početna
      </RouterLink>
      <RouterLink class="nav-item" to="/knjige">
        <span class="nav-icon"></span> Sve knjige
      </RouterLink>
      <RouterLink class="nav-item" to="#">
        <span class="nav-icon"></span> Pozajmice
      </RouterLink>
      <RouterLink class="nav-item" to="#">
        <span class="nav-icon"></span> Dugovanja
      </RouterLink>
      <RouterLink class="nav-item" to="#">
        <span class="nav-icon"></span> Obaveštenja
      </RouterLink>
      <RouterLink class="nav-item" to="/katalog">
        <span class="nav-icon"></span> Katalog
      </RouterLink>
      <RouterLink class="nav-item" to="/baze-podataka">
        <span class="nav-icon"></span> Elektronske baze podataka
      </RouterLink>

      <!-- Samo za clana -->
      <RouterLink v-if="role === 'CLAN'" class="nav-item" to="/moji-predlozi">
        <span class="nav-icon"></span> Moji predlozi naslova
      </RouterLink>
      <RouterLink v-if="role === 'CLAN'" class="nav-item" to="/notifikacije">
        <span class="nav-icon"></span> Praćenje statusa predloga
      </RouterLink>

      <!-- Samo za bibliotekara -->
      <RouterLink v-if="role === 'BIBLIOTEKAR'" class="nav-item" to="/predlozi-na-cekanju">
        <span class="nav-icon"></span> Predlozi naslova
      </RouterLink>
    </nav>

    <button class="logout-btn" @click="handleLogout">Odjavi se</button>
  </aside>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stroage/auth.js'

const router    = useRouter()
const authStore = useAuthStore()
const role = authStore.getRole()

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.logout-btn {
  margin: 0 1rem 1.5rem;
  background: transparent;
  border: 1px solid rgba(255,255,255,0.4);
  color: rgba(255,255,255,0.8);
  border-radius: 50px;
  padding: 0.4rem 1rem;
  cursor: pointer;
  font-size: 0.85rem;
  transition: background 0.2s;
}
.logout-btn:hover { background: rgba(255,255,255,0.1); }
</style>
