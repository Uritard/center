<template>
  <div id="app">
    <tool-index></tool-index>
  </div>
</template>

<script>
import ToolIndex from './view/ToolIndex.vue'

export default {
  name: 'App',
  components: {
    ToolIndex
  }
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}
</style>
