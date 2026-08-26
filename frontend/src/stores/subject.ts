import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getSubjects } from '@/api/subject'
import type { Subject } from '@/api/subject'

export const useSubjectStore = defineStore('subject', () => {
  const subjects = ref<Subject[]>([])
  const loading = ref(false)
  const loaded = ref(false)
  let pendingRequest: Promise<void> | undefined

  const subjectNames = computed(() => subjects.value.map(subject => subject.name))

  async function fetchSubjects(force = false) {
    if (loaded.value && !force) return
    if (pendingRequest && !force) return pendingRequest

    loading.value = true
    pendingRequest = getSubjects()
      .then(response => {
        subjects.value = response.data
        loaded.value = true
      })
      .finally(() => {
        loading.value = false
        pendingRequest = undefined
      })
    return pendingRequest
  }

  function invalidate() {
    loaded.value = false
  }

  return { subjects, subjectNames, loading, loaded, fetchSubjects, invalidate }
})
