//#include <jni.h>
//#include <android/log.h>
//#include "whisper.h"
//
//#define TAG "JNI"
//#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
//#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
//#define UNUSED(x) (void)(x)
//
//static inline int clamp_int(int v, int lo, int hi) { return v < lo ? lo : (v > hi ? hi : v); }
//
//extern "C" {
//
//
//JNIEXPORT jlong JNICALL
//Java_com_romeodev_whisper_WhisperLib_00024Companion_initContext(
//        JNIEnv* env, jobject thiz, jstring jModelPath) {
//    UNUSED(thiz);
//    const char* model_path = env->GetStringUTFChars(jModelPath, 0);
//    LOGI("Loading model: %s", model_path);
//
//    whisper_context_params wparams = whisper_context_default_params();
//    struct whisper_context* ctx = whisper_init_from_file_with_params(model_path, wparams);
//
//    env->ReleaseStringUTFChars(jModelPath, model_path);
//    if (!ctx) {
//        LOGW("Failed to load model");
//        return 0;
//    }
//    return (jlong) ctx;
//}
//
//JNIEXPORT void JNICALL
//Java_com_romeodev_whisper_WhisperLib_00024Companion_freeContext(
//        JNIEnv* env, jobject thiz, jlong context_ptr) {
//UNUSED(env); UNUSED(thiz);
//auto* ctx = (struct whisper_context*) context_ptr;
//if (ctx) whisper_free(ctx);
//}
//
//// ================= Full transcribe =================
//JNIEXPORT void JNICALL
//Java_com_romeodev_whisper_WhisperLib_00024Companion_fullTranscribe(
//        JNIEnv* env, jobject thiz,
//        jlong context_ptr, jint num_threads, jfloatArray jAudio /* mono 16kHz [-1..1] */) {
//UNUSED(thiz);
//auto* ctx = (struct whisper_context*) context_ptr;
//if (!ctx) { LOGW("Context is null"); return; }
//
//jfloat* audio = env->GetFloatArrayElements(jAudio, nullptr);
//const jsize n = env->GetArrayLength(jAudio);
//
//whisper_full_params params = whisper_full_default_params(WHISPER_SAMPLING_GREEDY);
//params.print_realtime   = false;
//params.print_progress   = false;
//params.print_timestamps = true;
//params.print_special    = false;
//params.translate        = false;
//params.language         = "auto";           // cambia a "es"/"en" si quieres forzar idioma
//params.n_threads        = clamp_int((int)num_threads, 1, 8);
//params.no_context       = true;
//params.single_segment   = false;
//params.offset_ms        = 0;
//
//whisper_reset_timings(ctx);
//LOGI("whisper_full: threads=%d, samples=%d", params.n_threads, (int)n);
//if (whisper_full(ctx, params, audio, (int)n) != 0) {
//LOGW("whisper_full failed");
//} else {
//whisper_print_timings(ctx);
//}
//
//env->ReleaseFloatArrayElements(jAudio, audio, JNI_ABORT);
//}
//
//
//JNIEXPORT jint JNICALL
//        Java_com_romeodev_whisper_WhisperLib_00024Companion_getTextSegmentCount(
//        JNIEnv* env, jobject thiz, jlong context_ptr) {
//UNUSED(env); UNUSED(thiz);
//auto* ctx = (struct whisper_context*) context_ptr;
//return ctx ? whisper_full_n_segments(ctx) : 0;
//}
//
//JNIEXPORT jstring JNICALL
//        Java_com_romeodev_whisper_WhisperLib_00024Companion_getTextSegment(
//        JNIEnv* env, jobject thiz, jlong context_ptr, jint index) {
//UNUSED(thiz);
//auto* ctx = (struct whisper_context*) context_ptr;
//if (!ctx) return env->NewStringUTF("");
//const char* text = whisper_full_get_segment_text(ctx, index);
//return env->NewStringUTF(text ? text : "");
//}
//
//JNIEXPORT jlong JNICALL
//        Java_com_romeodev_whisper_WhisperLib_00024Companion_getTextSegmentT0(
//        JNIEnv* env, jobject thiz, jlong context_ptr, jint index) {
//UNUSED(env); UNUSED(thiz);
//auto* ctx = (struct whisper_context*) context_ptr;
//return ctx ? whisper_full_get_segment_t0(ctx, index) : 0;
//}
//
//JNIEXPORT jlong JNICALL
//        Java_com_romeodev_whisper_WhisperLib_00024Companion_getTextSegmentT1(
//        JNIEnv* env, jobject thiz, jlong context_ptr, jint index) {
//UNUSED(env); UNUSED(thiz);
//auto* ctx = (struct whisper_context*) context_ptr;
//return ctx ? whisper_full_get_segment_t1(ctx, index) : 0;
//}
//
//// ================= Info / Bench (opcional) =================
//JNIEXPORT jstring JNICALL
//        Java_com_romeodev_whisper_WhisperLib_00024Companion_getSystemInfo(
//        JNIEnv* env, jobject thiz) {
//UNUSED(thiz);
//const char* s = whisper_print_system_info();
//return env->NewStringUTF(s ? s : "");
//}
//
//JNIEXPORT jstring JNICALL
//        Java_com_romeodev_whisper_WhisperLib_00024Companion_benchMemcpy(
//        JNIEnv* env, jobject thiz, jint n_threads) {
//UNUSED(thiz);
//const char* s = whisper_bench_memcpy_str(n_threads);
//return env->NewStringUTF(s ? s : "");
//}
//
//JNIEXPORT jstring JNICALL
//        Java_com_romeodev_whisper_WhisperLib_00024Companion_benchGgmlMulMat(
//        JNIEnv* env, jobject thiz, jint n_threads) {
//UNUSED(thiz);
//const char* s = whisper_bench_ggml_mul_mat_str(n_threads);
//return env->NewStringUTF(s ? s : "");
//}
//
//} // extern "C"
