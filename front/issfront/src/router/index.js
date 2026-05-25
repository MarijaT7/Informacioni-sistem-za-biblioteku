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
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.isLoggedIn) {
    next('/login')
  } else {
    next()
  }
})

export default router
