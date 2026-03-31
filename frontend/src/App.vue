<template>
  <div id="app">
    <el-container style="min-height: 100vh">
      <el-header v-if="$route.meta.showHeader !== false" style="background: #409eff; color: white; display: flex; align-items: center; padding: 0 20px;">
        <h2 style="margin: 0; cursor: pointer;" @click="$router.push('/')">FormCraft</h2>
        <el-menu :default-active="$route.path" mode="horizontal" background-color="#409eff" text-color="white" active-text-color="#ffd04b" router style="flex: 1; border: none;">
          <el-menu-item index="/">表单管理</el-menu-item>
        </el-menu>
      </el-header>
      <el-main style="background: #f5f7fa; min-height: calc(100vh - 60px);">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
}
</style>
