import { createRouter, createWebHistory } from 'vue-router'
import ForumIndex from '@/views/ForumIndex.vue' // 确保路径对应你刚写的组件
import Login from '@/views/users/Login.vue'
import Profile from '@/views/users/Profile.vue'
import HomeFeed from '@/components/index/HomeFeed.vue'
import PostDetail from '@/views/posts/PostDetail.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: ForumIndex, // 访问 http://localhost:5173/ 直接看到大论坛首页
    children: [
      {
        path: '', // 默认访问 '/' 时展示列表和右边栏
        name: 'HomeFeed',
        component: HomeFeed
      },
      {
        path: 'post/:id', // 访问 '/post/123' 时展示详情
        name: 'PostDetail',
        component: PostDetail // 此时 HomeFeed 卸载，PostDetail 渲染在右侧
      }
    ]
  },
  {
    path: '/profile',
    name: 'Profile',
    component: Profile // 访问 http://localhost:5173/prorile 进入管理页
  },
  {
    path: '/login',
    name: 'Login',
    component: Login // 访问 http://localhost:5173/admin/posts 进入管理页
  },
  {
    path: '/register',
    name: 'Register',
    component: ()=>import('@/views/users/Register.vue'), // 访问 http://localhost:5173/admin/posts 进入管理页
    props: true
  },
  {
    path: '/forget-password',
    name: 'ForgetPassword',
    component: ()=>import('@/views/users/ForgetPassword.vue'), // 访问 http://localhost:5173/admin/posts 进入管理页
    props: true
  },
  {
    path: '/board/:id',
    name: 'BoardDetail',
    component: ()=>import('@/views/boards/BoardDetail.vue'), // 访问 http://localhost:5173/admin/posts 进入管理页
    props: true
  },
  {
    path: '/board-square',
    name: 'BoardSquare',
    component: ()=>import('@/views/boards/BoardSquare.vue'), // 访问 http://localhost:5173/admin/posts 进入管理页
    props: true
  },
  {
    path: '/message-detail',
    name: 'MessageDetail',
    component: ()=>import('@/views/messages/MessageDetail.vue'), // 访问 http://localhost:5173/admin/posts 进入管理页
    props: true
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router