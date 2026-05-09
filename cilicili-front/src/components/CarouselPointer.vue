<template>
    <div class="component-24_226">
        <div id="24_226" class="symbol-24_226">
            <div id="24_225" class="symbol-24_225" v-if="属性1 === '默认'">
                <div id="24_222" class="vector-24_222"></div>
            </div>
            <div id="24_224" class="symbol-24_224" v-if="属性1 === '选中'">
                <div id="24_223" class="vector-24_223"></div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
const props = defineProps({
    属性1: { type: String }
});
</script>
<style>
.symbol-24_226 {
}
.symbol-24_225 {
    width: 0.75rem;
    height: 0.75rem;
    position: relative;
    background-color: transparent;
}
.vector-24_222 {
    width: 100%;
    height: 100%;
    background-image: url(@/assets/images/carousel1.png);
    background-size: 100% 100%;
    background-repeat: no-repeat;
    position: absolute;
    left: 0%;
    right: 0%;
    top: 0%;
    bottom: 0%;
}
.symbol-24_224 {
    width: 1.25rem;
    height: 1.25rem;
    position: relative;
    background-color: transparent;
}
.vector-24_223 {
    width: 100%;
    height: 100%;
    background-image: url(@/assets/images/carousel0.png);
    background-size: 100% 100%;
    background-repeat: no-repeat;
    position: absolute;
    left: 0%;
    right: 0%;
    top: 0%;
    bottom: 0%;
}
</style>
