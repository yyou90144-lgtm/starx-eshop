<script setup>
import AuthForm from '../../components/AuthForm.vue'
</script>

<template>
  <div class="auth-page pc-auth-page">
    <AuthForm
      terminal="pc"
      title="星选商城"
      login-subtitle="登录后同步购物车、订单与收藏"
      :registration-roles="['USER', 'SELLER']"
    />
  </div>
</template>
