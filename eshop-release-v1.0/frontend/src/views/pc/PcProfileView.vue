<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { updateProfile } from '../../api/auth'
import { useAuthStore } from '../../stores/auth'

const auth = useAuthStore()
const loadingProfile = ref(false)
const saving = ref(false)
const errorMessage = ref('')
const nickname = ref('')
const phone = ref('')

const phoneValid = computed(() => (
  !phone.value.trim() || /^[0-9+\- ]{6,20}$/.test(phone.value.trim())
))

const canSave = computed(() => {
  const normalizedNickname = nickname.value.trim()
  return (
    !loadingProfile.value
    && !saving.value
    && normalizedNickname.length > 0
    && normalizedNickname.length <= 50
    && phoneValid.value
  )
})

const displayName = computed(() => (
  nickname.value.trim()
  || auth.user?.nickname
  || auth.user?.username
  || '星选用户'
))

const avatarLetter = computed(() => {
  const name = displayName.value.trim()
  return name ? name.slice(0, 1).toUpperCase() : '星'
})

const roleLabel = computed(() => {
  if (auth.isAdmin) return '平台管理员'
  if (auth.isSeller) return '商家'
  return '买家会员'
})

const roleTone = computed(() => {
  if (auth.isAdmin) return 'admin'
  if (auth.isSeller) return 'seller'
  return 'buyer'
})

const applyUserToForm = (user) => {
  nickname.value = user?.nickname || ''
  phone.value = user?.phone || ''
}

const loadProfile = async () => {
  if (loadingProfile.value) return
  loadingProfile.value = true
  errorMessage.value = ''
  try {
    const user = await auth.refreshCurrentUser()
    applyUserToForm(user)
  } catch (error) {
    errorMessage.value = error.message || '个人资料加载失败'
    applyUserToForm(auth.user)
  } finally {
    loadingProfile.value = false
  }
}

const saveProfile = async () => {
  if (!canSave.value) {
    ElMessage.warning(phoneValid.value ? '请填写有效昵称' : '联系电话格式不正确')
    return
  }
  saving.value = true
  try {
    const user = await updateProfile({
      nickname: nickname.value.trim(),
      phone: phone.value.trim(),
    })
    auth.user = user
    localStorage.setItem('eshop_user', JSON.stringify(user))
    applyUserToForm(user)
    ElMessage.success('个人资料已保存')
  } catch (error) {
    ElMessage.error(error.message || '保存失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

onMounted(loadProfile)
</script>

<template>
  <section class="pc-profile" aria-labelledby="profile-title">
    <header class="profile-heading">
      <div class="heading-copy">
        <p class="eyebrow">账户设置</p>
        <h1 id="profile-title">个人资料</h1>
        <p class="heading-desc">维护昵称与联系方式，便于订单沟通与售后联系。</p>
      </div>
      <el-button class="reload-btn" :loading="loadingProfile" @click="loadProfile">
        重新加载
      </el-button>
    </header>

    <el-alert
      v-if="errorMessage"
      class="error-alert"
      type="error"
      :title="errorMessage"
      show-icon
      :closable="false"
    />

    <div class="profile-layout" v-loading="loadingProfile">
      <aside class="identity-panel">
        <div class="identity-card">
          <div class="avatar-mark" aria-hidden="true">{{ avatarLetter }}</div>
          <div class="identity-meta">
            <h2>{{ displayName }}</h2>
            <p class="username">@{{ auth.user?.username || '未登录' }}</p>
            <span class="role-chip" :class="roleTone">{{ roleLabel }}</span>
          </div>
        </div>

        <nav class="quick-links" aria-label="账户快捷入口">
          <RouterLink class="quick-link" to="/pc/orders">
            <span class="ql-label">我的订单</span>
            <span class="ql-hint">查看物流与支付状态</span>
          </RouterLink>
          <RouterLink class="quick-link" to="/pc/addresses">
            <span class="ql-label">收货地址</span>
            <span class="ql-hint">管理常用配送地址</span>
          </RouterLink>
          <RouterLink class="quick-link" to="/pc/favorites">
            <span class="ql-label">我的收藏</span>
            <span class="ql-hint">稍后继续挑选好物</span>
          </RouterLink>
          <RouterLink class="quick-link" to="/pc/reviews">
            <span class="ql-label">我的评价</span>
            <span class="ql-hint">查看已提交的商品评价</span>
          </RouterLink>
        </nav>
      </aside>

      <div class="profile-card">
        <div class="card-head">
          <h3>基本信息</h3>
          <p>带 * 的字段为必填；用户名创建后不可修改。</p>
        </div>

        <el-form
          class="profile-form"
          label-position="top"
          size="large"
          @submit.prevent="saveProfile"
        >
          <el-form-item label="用户名">
            <el-input :model-value="auth.user?.username" disabled />
            <div class="form-tip">用于登录的唯一账号，创建后不可修改。</div>
          </el-form-item>

          <el-form-item label="昵称" required>
            <el-input
              v-model="nickname"
              placeholder="例如：星选小主"
              maxlength="50"
              show-word-limit
            />
          </el-form-item>

          <el-form-item
            label="联系电话"
            :error="phoneValid ? '' : '请输入 6—20 位数字、空格、+ 或 -'"
          >
            <el-input
              v-model="phone"
              placeholder="手机号或固定电话"
              maxlength="20"
            />
            <div class="form-tip">选填。填写后便于订单与售后联系。</div>
          </el-form-item>

          <el-form-item label="账户角色">
            <span class="role-chip inline" :class="roleTone">{{ roleLabel }}</span>
          </el-form-item>

          <div class="form-actions">
            <el-button
              type="primary"
              native-type="submit"
              class="save-btn"
              :loading="saving"
              :disabled="!canSave"
            >
              保存修改
            </el-button>
            <button
              type="button"
              class="text-reset"
              :disabled="loadingProfile || saving"
              @click="applyUserToForm(auth.user)"
            >
              还原未保存修改
            </button>
          </div>
        </el-form>
      </div>
    </div>
  </section>
</template>

<style scoped>
.pc-profile {
  width: min(var(--eshop-container, 1280px), calc(100% - 40px));
  margin: 0 auto;
  padding: 8px 0 48px;
}

.profile-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 22px;
}

.heading-copy {
  min-width: 0;
}

.eyebrow {
  margin: 0 0 6px;
  color: #ff4d4f;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: .14em;
}

.profile-heading h1 {
  margin: 0 0 8px;
  color: #1c1917;
  font-size: clamp(26px, 3vw, 32px);
  font-weight: 800;
  letter-spacing: -.02em;
  line-height: 1.15;
  text-wrap: balance;
}

.heading-desc {
  margin: 0;
  max-width: 36em;
  color: #8a837c;
  font-size: 14px;
  line-height: 1.6;
}

.reload-btn {
  flex: 0 0 auto;
  border-radius: 12px !important;
  border-color: #ebe4de !important;
  color: #3f3a36 !important;
  background: #fff !important;
  font-weight: 600;
  transition: transform .18s ease, border-color .18s ease, color .18s ease;
}

.reload-btn:hover {
  border-color: #ffd6d2 !important;
  color: #ff4d4f !important;
}

.error-alert {
  margin-bottom: 16px;
  border-radius: 12px;
}

.profile-layout {
  display: grid;
  grid-template-columns: minmax(240px, 300px) minmax(0, 1fr);
  gap: 18px;
  align-items: start;
}

.identity-panel {
  display: grid;
  gap: 14px;
}

.identity-card {
  display: grid;
  gap: 16px;
  justify-items: start;
  padding: 22px 20px 20px;
  background:
    radial-gradient(circle at 100% 0%, rgba(255, 77, 79, .1), transparent 42%),
    #fff;
  border: 1px solid #ebe4de;
  border-radius: 18px;
  box-shadow: 0 4px 16px rgba(28, 20, 16, .04);
}

.avatar-mark {
  display: grid;
  width: 64px;
  height: 64px;
  place-items: center;
  color: #fff;
  background: #ff4d4f;
  border-radius: 18px;
  font-size: 26px;
  font-weight: 800;
  letter-spacing: -.02em;
  box-shadow: 0 12px 24px rgba(255, 77, 79, .22);
}

.identity-meta h2 {
  margin: 0 0 4px;
  color: #1c1917;
  font-size: 20px;
  font-weight: 750;
  letter-spacing: -.01em;
  line-height: 1.25;
  word-break: break-word;
}

.username {
  margin: 0 0 12px;
  color: #8a837c;
  font-size: 13px;
  font-variant-numeric: tabular-nums;
}

.role-chip {
  display: inline-flex;
  align-items: center;
  min-height: 26px;
  padding: 0 10px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: .02em;
}

.role-chip.buyer {
  color: #cf1322;
  background: #fff1f0;
}

.role-chip.seller {
  color: #a16207;
  background: #fffbeb;
}

.role-chip.admin {
  color: #1c1917;
  background: #f3ece6;
}

.role-chip.inline {
  margin-top: 2px;
}

.quick-links {
  display: grid;
  gap: 8px;
  padding: 12px;
  background: #fff;
  border: 1px solid #ebe4de;
  border-radius: 18px;
  box-shadow: 0 4px 16px rgba(28, 20, 16, .04);
}

.quick-link {
  display: grid;
  gap: 2px;
  padding: 12px 12px;
  border-radius: 12px;
  text-decoration: none;
  transition: background .18s ease, transform .18s ease;
}

.quick-link:hover {
  background: #faf7f5;
  transform: translateX(2px);
}

.quick-link:focus-visible {
  outline: 2px solid #ff4d4f;
  outline-offset: 2px;
}

.ql-label {
  color: #1c1917;
  font-size: 14px;
  font-weight: 650;
}

.ql-hint {
  color: #8a837c;
  font-size: 12px;
  line-height: 1.4;
}

.profile-card {
  padding: 26px 28px 28px;
  background: #fff;
  border: 1px solid #ebe4de;
  border-radius: 18px;
  box-shadow: 0 4px 16px rgba(28, 20, 16, .04);
}

.card-head {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0ebe6;
}

.card-head h3 {
  margin: 0 0 6px;
  color: #1c1917;
  font-size: 18px;
  font-weight: 750;
  letter-spacing: -.01em;
}

.card-head p {
  margin: 0;
  color: #8a837c;
  font-size: 13px;
  line-height: 1.5;
}

.profile-form :deep(.el-form-item__label) {
  color: #3f3a36;
  font-weight: 600;
}

.profile-form :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 0 0 1px #ebe4de inset;
  transition: box-shadow .18s ease;
}

.profile-form :deep(.el-input__wrapper:hover),
.profile-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #ffb3a7 inset !important;
}

.profile-form :deep(.el-input.is-disabled .el-input__wrapper) {
  background: #faf7f5;
}

.form-tip {
  margin-top: 6px;
  color: #8a837c;
  font-size: 12px;
  line-height: 1.45;
}

.form-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 14px;
  margin-top: 8px;
  padding-top: 8px;
}

.save-btn {
  min-width: 120px;
  border-radius: 12px !important;
  font-weight: 700 !important;
  transition: transform .15s ease, filter .15s ease !important;
}

.save-btn:hover:not(:disabled) {
  filter: brightness(1.03);
}

.save-btn:active:not(:disabled) {
  transform: scale(0.98);
}

.text-reset {
  padding: 0;
  border: 0;
  background: none;
  color: #8a837c;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: color .18s ease;
}

.text-reset:hover:not(:disabled) {
  color: #ff4d4f;
}

.text-reset:disabled {
  opacity: .5;
  cursor: not-allowed;
}

.text-reset:focus-visible {
  outline: 2px solid #ff4d4f;
  outline-offset: 3px;
  border-radius: 4px;
}

@media (max-width: 900px) {
  .profile-layout {
    grid-template-columns: 1fr;
  }

  .identity-card {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .identity-meta h2 {
    font-size: 18px;
  }
}

@media (max-width: 640px) {
  .profile-heading {
    flex-direction: column;
    align-items: flex-start;
  }

  .profile-card {
    padding: 20px 16px 22px;
  }

  .form-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .save-btn {
    width: 100%;
  }
}
.pc-profile {
  padding-top: 16px;
}

.profile-heading {
  align-items: flex-start;
  margin-bottom: 20px;
}

.eyebrow {
  display: none;
}

.profile-heading h1 {
  margin-bottom: 6px;
  color: var(--eshop-text);
  font-size: 30px;
  letter-spacing: 0;
}

.heading-desc {
  color: var(--eshop-muted);
}

.reload-btn {
  border-color: var(--eshop-border) !important;
  border-radius: 8px !important;
  color: var(--eshop-text-soft) !important;
  background: var(--eshop-surface) !important;
}

.identity-card,
.quick-links,
.profile-card {
  background: var(--eshop-surface) !important;
  border-color: var(--eshop-border) !important;
  border-radius: var(--eshop-radius) !important;
  box-shadow: var(--eshop-shadow-sm) !important;
}

.identity-card {
  background: var(--eshop-surface) !important;
}

.avatar-mark {
  border-radius: 10px;
  background: var(--eshop-primary);
  box-shadow: none;
  letter-spacing: 0;
}

.identity-meta h2,
.ql-label,
.card-head h3 {
  color: var(--eshop-text);
  letter-spacing: 0;
}

.username,
.ql-hint,
.card-head p,
.form-tip,
.text-reset {
  color: var(--eshop-muted);
}

.quick-link:hover {
  background: var(--eshop-surface-soft);
  transform: none;
}

.quick-link:focus-visible,
.text-reset:focus-visible {
  outline-color: var(--eshop-primary);
}

.role-chip {
  letter-spacing: 0;
}

.role-chip.seller {
  color: #a15c00;
  background: #fff7e8;
}

.role-chip.admin {
  color: var(--eshop-text-soft);
  background: var(--eshop-surface-soft);
}

.card-head {
  border-bottom-color: var(--eshop-border);
}

.profile-form :deep(.el-form-item__label) {
  color: var(--eshop-text-soft);
}

.profile-form :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px var(--eshop-border) inset;
}

.profile-form :deep(.el-input__wrapper:hover),
.profile-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #ffaaa8 inset !important;
}

.profile-form :deep(.el-input.is-disabled .el-input__wrapper) {
  background: var(--eshop-surface-soft);
}

.save-btn {
  border-radius: 8px !important;
}

@media (max-width: 640px) {
  .pc-profile {
    width: min(100% - 24px, var(--eshop-container, 1280px));
  }
}
</style>
