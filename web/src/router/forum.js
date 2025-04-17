export default {
  path: '/forum',
  name: 'Forum',
  redirect: '/forum/list',
  component: () => import('@/views/web/Home.vue'),
  meta: {
    requireAuth: false,
    locale: '非遗论坛',
    icon: 'icon-message',
  },
  children: [
    {
      path: 'list',
      name: 'ForumList',
      component: () => import('@/views/web/pages/forum/ForumList.vue'),
      meta: {
        requireAuth: false,
        title: '非遗论坛',
      },
    },
    {
      path: 'detail/:id',
      name: 'ForumDetail',
      component: () => import('@/views/web/pages/forum/PostDetail.vue'),
      meta: {
        requireAuth: false,
        title: '帖子详情',
      },
    },
    {
      path: 'publish',
      name: 'PublishPost',
      component: () => import('@/views/web/pages/forum/PostCreate.vue'),
      meta: {
        requireAuth: true,
        title: '发布帖子',
      },
    },
    {
      path: 'edit/:id',
      name: 'EditPost',
      component: () => import('@/views/web/pages/forum/PostCreate.vue'),
      meta: {
        requireAuth: true,
        title: '编辑帖子',
      },
    },
    {
      path: 'post/:id',
      name: 'PostDetail',
      component: () => import('@/views/web/pages/forum/PostDetail.vue'),
      meta: {
        requireAuth: false,
        title: '帖子详情',
      },
    },
    {
      path: 'create',
      name: 'CreatePost',
      component: () => import('@/views/web/pages/forum/PostCreate.vue'),
      meta: {
        requireAuth: true,
        title: '发布帖子',
      },
    },
  ],
} 