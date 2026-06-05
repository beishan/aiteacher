import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getCurrentUser } from '@/api/auth'
import type { LoginResult } from '@/api/auth'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<LoginResult | null>(
    JSON.parse(localStorage.getItem('user') || 'null')
  )

  const isLoggedIn = computed(() => !!token.value)
  const displayName = computed(() => userInfo.value?.displayName || userInfo.value?.username || '')

  async function login(username: string, password: string) {
    const res = await loginApi({ username, password })
    token.value = res.data.token
    userInfo.value = res.data
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('user', JSON.stringify(res.data))
  }

  async function fetchUserInfo() {
    const res = await getCurrentUser()
    userInfo.value = res.data
    localStorage.setItem('user', JSON.stringify(res.data))
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    router.push('/login')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    displayName,
    login,
    fetchUserInfo,
    logout,
  }
})
