import { createRouter, createWebHistory } from 'vue-router'
import FormList from '../views/FormList.vue'
import FormDesigner from '../views/FormDesigner.vue'
import FormFill from '../views/FormFill.vue'
import FormData from '../views/FormData.vue'

const routes = [
  {
    path: '/',
    name: 'FormList',
    component: FormList
  },
  {
    path: '/design/:id?',
    name: 'FormDesigner',
    component: FormDesigner,
    props: true
  },
  {
    path: '/fill/:id',
    name: 'FormFill',
    component: FormFill,
    props: true,
    meta: { showHeader: false }
  },
  {
    path: '/data/:id',
    name: 'FormData',
    component: FormData,
    props: true
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
