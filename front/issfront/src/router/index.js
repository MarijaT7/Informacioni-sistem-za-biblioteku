import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stroage/auth.js'

const routes = [
  { path: '/',              redirect: '/login' },
  { path: '/login',         component: () => import('../views/LoginView.vue')     },
  { path: '/register',      component: () => import('../views/RegisterStep1.vue') },
  { path: '/register/step2',component: () => import('../views/RegisterStep2.vue') },
  { path: '/register/step3',component: () => import('../views/RegisterStep3.vue') },
  { path: '/register/genres',component: () => import('../views/FavouriteGenres.vue') },
  {
    path: '/profile',
    component: () => import('../views/UserProfile.vue'),
    meta: { requiresAuth: true }
  },
  {
      path: '/genres/edit',
      component: () => import('../views/FavouriteGenresEdit.vue'),
      meta: { requiresAuth: true }
    },
  path: '/menadzer',
  component: () => import('../views/menadzer/AppLayout.vue'),
  meta: { requiresAuth: true },
  meta: { requiresAuth: true, requiresRole: 'MENADZER' },
  children: [
    { path: '',                    redirect: '/menadzer/dashboard' },
    { path: 'dashboard',           component: () => import('../views/menadzer/DashboardView.vue') },
    { path: 'dobavljaci',          component: () => import('../views/menadzer/DobavljaciView.vue') },
    { path: 'dobavljaci/novi',     component: () => import('../views/menadzer/DodajDobavljacaView.vue') },
    { path: 'dobavljaci/:id',      component: () => import('../views/menadzer/DobavljacDetaljiView.vue') },
    { path: 'dobavljaci/:id/izmena', component: () => import('../views/menadzer/IzmenaDobavljacaView.vue') },
    { path: 'dobavljaci/:id/ugovor', component: () => import('../views/menadzer/DodajUgovorView.vue') },

  ]
},
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.isLoggedIn) {
    next('/login')
    return
  }

  if (to.meta.requiresRole && auth.user?.role !== to.meta.requiresRole) {
    next('/login')
    return
  }

  next()
})

export default router
