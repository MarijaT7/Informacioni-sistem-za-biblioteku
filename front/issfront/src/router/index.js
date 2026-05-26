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
    path: '/knjige',
    component: () => import('../views/BooksView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/knjige/:isbn',
    component: () => import('../views/BookDetailView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/knjige/:isbn/citaj',
    component: () => import('../views/BookReadView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/knjige/:isbn/slusaj',
    component: () => import('../views/BookListenView.vue'),
    meta: { requiresAuth: true }
  },
    { path: '/katalog/novi',  component: () => import('../views/NoviKatalog.vue'), beforeEnter: () => {
      const auth = useAuthStore()
      return auth.getRole() === 'BIBLIOTEKAR' ? true : '/'
    }},
  {
      path: '/genres/edit',
      component: () => import('../views/FavouriteGenresEdit.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/katalog',
      component: () => import('../views/KatalogView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/katalog/:id',
      component: () => import('../views/KatalogDetaljiView.vue'),
      meta: { requiresAuth: true }
    },

  {
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

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.isLoggedIn) {
    return '/login'
  }

  if (to.meta.requiresRole && auth.user?.role !== to.meta.requiresRole) {
    return '/login'
  }
})

export default router
