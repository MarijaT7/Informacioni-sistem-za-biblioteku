<template>
  <div id="app">
    <div id="center">
      <div v-if="!authorized">
        <p>Nemate pristup ovoj stranici.</p>
      </div>
      <div v-else style="width: 100%; max-width: 480px; text-align: left; padding: 32px">
        <h1>Novi katalog</h1>
        <div style="display: flex; flex-direction: column; gap: 16px">
          <div>
            <label style="display: block; margin-bottom: 6px">Naziv</label>
            <input v-model="naziv" type="text" placeholder="Naziv kataloga"
              style="width: 100%; padding: 8px 12px; border: 1px solid var(--border); border-radius: 6px; background: var(--bg); color: var(--text-h); font-size: 16px; box-sizing: border-box" />
          </div>
          <div>
            <label style="display: block; margin-bottom: 6px">Standard</label>
            <input v-model="standard" type="text" placeholder="npr. MARC"
              style="width: 100%; padding: 8px 12px; border: 1px solid var(--border); border-radius: 6px; background: var(--bg); color: var(--text-h); font-size: 16px; box-sizing: border-box" />
          </div>
          <p v-if="error" style="color: red">{{ error }}</p>
          <p v-if="success" style="color: green">{{ success }}</p>
          <button @click="submit" class="counter" style="cursor: pointer; border: none; padding: 10px 20px; font-size: 16px">
            Dodaj katalog
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useAuthStore } from '../stroage/auth.js'

const auth = useAuthStore()
const naziv = ref('')
const standard = ref('')
const error = ref('')
const success = ref('')
const authorized = ref(false)

onMounted(() => {
    authorized.value = auth.getRole() === 'BIBLIOTEKAR'
})

async function submit() {
    error.value = ''
    success.value = ''
    if (!naziv.value || !standard.value) {
        error.value = 'Sva polja su obavezna.'
        return
    }
    try {
        await axios.post('http://localhost:8080/api/katalog/novi', {
            naziv: naziv.value,
            standard: standard.value
        }, {
            headers: { Authorization: `Bearer ${auth.token}` }
        })
        success.value = 'Katalog uspesno dodat.'
        naziv.value = ''
        standard.value = ''
    } catch (e) {
        error.value = e.response?.data || 'Doslo je do greske.'
    }
}
</script>
