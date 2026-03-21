#include <jni.h>
#include <android/log.h>
#include <stdlib.h>
#include <fluidsynth.h>

#define LOG_TAG "FluidSynthJNI"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

/* --------------------------------------------------------------------------
 * Package: dev.kotlinds.fluidsynthkmp
 * JNI naming: Java_dev_kotlinds_fluidsynthkmp_FluidSynthJni_*
 * -------------------------------------------------------------------------- */

/* ---- Settings ---- */

JNIEXPORT jlong JNICALL
Java_dev_kotlinds_fluidsynthkmp_FluidSynthJni_newSettings(JNIEnv *env, jobject thiz) {
    fluid_settings_t *settings = new_fluid_settings();
    if (!settings) {
        LOGE("new_fluid_settings() failed");
        return 0L;
    }
    return (jlong)(intptr_t)settings;
}

JNIEXPORT void JNICALL
Java_dev_kotlinds_fluidsynthkmp_FluidSynthJni_deleteSettings(JNIEnv *env, jobject thiz, jlong settings_ptr) {
    if (settings_ptr) {
        delete_fluid_settings((fluid_settings_t *)(intptr_t)settings_ptr);
    }
}

JNIEXPORT jint JNICALL
Java_dev_kotlinds_fluidsynthkmp_FluidSynthJni_setSettingsNum(JNIEnv *env, jobject thiz, jlong settings_ptr, jstring name, jdouble value) {
    if (!settings_ptr) return -1;
    const char *c_name = (*env)->GetStringUTFChars(env, name, NULL);
    int result = fluid_settings_setnum((fluid_settings_t *)(intptr_t)settings_ptr, c_name, value);
    (*env)->ReleaseStringUTFChars(env, name, c_name);
    return result;
}

JNIEXPORT jint JNICALL
Java_dev_kotlinds_fluidsynthkmp_FluidSynthJni_setSettingsInt(JNIEnv *env, jobject thiz, jlong settings_ptr, jstring name, jint value) {
    if (!settings_ptr) return -1;
    const char *c_name = (*env)->GetStringUTFChars(env, name, NULL);
    int result = fluid_settings_setint((fluid_settings_t *)(intptr_t)settings_ptr, c_name, value);
    (*env)->ReleaseStringUTFChars(env, name, c_name);
    return result;
}

/* ---- Synth ---- */

JNIEXPORT jlong JNICALL
Java_dev_kotlinds_fluidsynthkmp_FluidSynthJni_newSynth(JNIEnv *env, jobject thiz, jlong settings_ptr) {
    if (!settings_ptr) return 0L;
    fluid_synth_t *synth = new_fluid_synth((fluid_settings_t *)(intptr_t)settings_ptr);
    if (!synth) {
        LOGE("new_fluid_synth() failed");
        return 0L;
    }
    return (jlong)(intptr_t)synth;
}

JNIEXPORT void JNICALL
Java_dev_kotlinds_fluidsynthkmp_FluidSynthJni_deleteSynth(JNIEnv *env, jobject thiz, jlong synth_ptr) {
    if (synth_ptr) {
        delete_fluid_synth((fluid_synth_t *)(intptr_t)synth_ptr);
    }
}

/* ---- Audio Driver ---- */

JNIEXPORT jlong JNICALL
Java_dev_kotlinds_fluidsynthkmp_FluidSynthJni_newAudioDriver(JNIEnv *env, jobject thiz, jlong settings_ptr, jlong synth_ptr) {
    if (!settings_ptr || !synth_ptr) return 0L;
    fluid_audio_driver_t *driver = new_fluid_audio_driver(
        (fluid_settings_t *)(intptr_t)settings_ptr,
        (fluid_synth_t *)(intptr_t)synth_ptr
    );
    if (!driver) {
        LOGE("new_fluid_audio_driver() failed");
        return 0L;
    }
    return (jlong)(intptr_t)driver;
}

JNIEXPORT void JNICALL
Java_dev_kotlinds_fluidsynthkmp_FluidSynthJni_deleteDriver(JNIEnv *env, jobject thiz, jlong driver_ptr) {
    if (driver_ptr) {
        delete_fluid_audio_driver((fluid_audio_driver_t *)(intptr_t)driver_ptr);
    }
}

/* ---- SoundFont ---- */

JNIEXPORT jint JNICALL
Java_dev_kotlinds_fluidsynthkmp_FluidSynthJni_sfLoad(JNIEnv *env, jobject thiz, jlong synth_ptr, jstring path, jboolean reset_presets) {
    if (!synth_ptr) return -1;
    const char *c_path = (*env)->GetStringUTFChars(env, path, NULL);
    int sfid = fluid_synth_sfload((fluid_synth_t *)(intptr_t)synth_ptr, c_path, reset_presets ? 1 : 0);
    (*env)->ReleaseStringUTFChars(env, path, c_path);
    return sfid;
}

/* ---- Interpolation ---- */

JNIEXPORT jint JNICALL
Java_dev_kotlinds_fluidsynthkmp_FluidSynthJni_setInterpMethod(JNIEnv *env, jobject thiz, jlong synth_ptr, jint chan, jint interp_method) {
    if (!synth_ptr) return -1;
    return fluid_synth_set_interp_method((fluid_synth_t *)(intptr_t)synth_ptr, chan, interp_method);
}

/* ---- Note Control ---- */

JNIEXPORT void JNICALL
Java_dev_kotlinds_fluidsynthkmp_FluidSynthJni_noteOn(JNIEnv *env, jobject thiz, jlong synth_ptr, jint channel, jint key, jint velocity) {
    if (synth_ptr) {
        fluid_synth_noteon((fluid_synth_t *)(intptr_t)synth_ptr, channel, key, velocity);
    }
}

JNIEXPORT void JNICALL
Java_dev_kotlinds_fluidsynthkmp_FluidSynthJni_noteOff(JNIEnv *env, jobject thiz, jlong synth_ptr, jint channel, jint key) {
    if (synth_ptr) {
        fluid_synth_noteoff((fluid_synth_t *)(intptr_t)synth_ptr, channel, key);
    }
}

JNIEXPORT void JNICALL
Java_dev_kotlinds_fluidsynthkmp_FluidSynthJni_programChange(JNIEnv *env, jobject thiz, jlong synth_ptr, jint channel, jint program) {
    if (synth_ptr) {
        fluid_synth_program_change((fluid_synth_t *)(intptr_t)synth_ptr, channel, program);
    }
}

JNIEXPORT void JNICALL
Java_dev_kotlinds_fluidsynthkmp_FluidSynthJni_setGain(JNIEnv *env, jobject thiz, jlong synth_ptr, jfloat gain) {
    if (synth_ptr) {
        fluid_synth_set_gain((fluid_synth_t *)(intptr_t)synth_ptr, gain);
    }
}

/* ---- MIDI Player ---- */

JNIEXPORT jlong JNICALL
Java_dev_kotlinds_fluidsynthkmp_FluidSynthJni_playerNew(JNIEnv *env, jobject thiz, jlong synth_ptr) {
    if (!synth_ptr) return 0L;
    fluid_player_t *player = new_fluid_player((fluid_synth_t *)(intptr_t)synth_ptr);
    if (!player) {
        LOGE("new_fluid_player() failed");
        return 0L;
    }
    return (jlong)(intptr_t)player;
}

JNIEXPORT jint JNICALL
Java_dev_kotlinds_fluidsynthkmp_FluidSynthJni_playerAdd(JNIEnv *env, jobject thiz, jlong player_ptr, jstring midi_path) {
    if (!player_ptr) return -1;
    const char *c_path = (*env)->GetStringUTFChars(env, midi_path, NULL);
    int result = fluid_player_add((fluid_player_t *)(intptr_t)player_ptr, c_path);
    (*env)->ReleaseStringUTFChars(env, midi_path, c_path);
    return result;
}

JNIEXPORT jint JNICALL
Java_dev_kotlinds_fluidsynthkmp_FluidSynthJni_playerPlay(JNIEnv *env, jobject thiz, jlong player_ptr) {
    if (!player_ptr) return -1;
    return fluid_player_play((fluid_player_t *)(intptr_t)player_ptr);
}

JNIEXPORT jint JNICALL
Java_dev_kotlinds_fluidsynthkmp_FluidSynthJni_playerStop(JNIEnv *env, jobject thiz, jlong player_ptr) {
    if (!player_ptr) return -1;
    return fluid_player_stop((fluid_player_t *)(intptr_t)player_ptr);
}

JNIEXPORT void JNICALL
Java_dev_kotlinds_fluidsynthkmp_FluidSynthJni_playerDelete(JNIEnv *env, jobject thiz, jlong player_ptr) {
    if (player_ptr) {
        delete_fluid_player((fluid_player_t *)(intptr_t)player_ptr);
    }
}

JNIEXPORT jint JNICALL
Java_dev_kotlinds_fluidsynthkmp_FluidSynthJni_playerGetStatus(JNIEnv *env, jobject thiz, jlong player_ptr) {
    if (!player_ptr) return 0; /* FLUID_PLAYER_DONE = 0 */
    return fluid_player_get_status((fluid_player_t *)(intptr_t)player_ptr);
}

JNIEXPORT jint JNICALL
Java_dev_kotlinds_fluidsynthkmp_FluidSynthJni_playerJoin(JNIEnv *env, jobject thiz, jlong player_ptr) {
    if (!player_ptr) return -1;
    return fluid_player_join((fluid_player_t *)(intptr_t)player_ptr);
}

/* ---- Reverb ---- */

JNIEXPORT jint JNICALL
Java_dev_kotlinds_fluidsynthkmp_FluidSynthJni_setReverbRoomSize(JNIEnv *env, jobject thiz, jlong synth_ptr, jdouble room_size) {
    if (!synth_ptr) return -1;
    return fluid_synth_set_reverb_group_roomsize((fluid_synth_t *)(intptr_t)synth_ptr, -1, room_size);
}

JNIEXPORT jint JNICALL
Java_dev_kotlinds_fluidsynthkmp_FluidSynthJni_setReverbDamp(JNIEnv *env, jobject thiz, jlong synth_ptr, jdouble damping) {
    if (!synth_ptr) return -1;
    return fluid_synth_set_reverb_group_damp((fluid_synth_t *)(intptr_t)synth_ptr, -1, damping);
}

JNIEXPORT jint JNICALL
Java_dev_kotlinds_fluidsynthkmp_FluidSynthJni_setReverbWidth(JNIEnv *env, jobject thiz, jlong synth_ptr, jdouble width) {
    if (!synth_ptr) return -1;
    return fluid_synth_set_reverb_group_width((fluid_synth_t *)(intptr_t)synth_ptr, -1, width);
}

JNIEXPORT jint JNICALL
Java_dev_kotlinds_fluidsynthkmp_FluidSynthJni_setReverbLevel(JNIEnv *env, jobject thiz, jlong synth_ptr, jdouble level) {
    if (!synth_ptr) return -1;
    return fluid_synth_set_reverb_group_level((fluid_synth_t *)(intptr_t)synth_ptr, -1, level);
}

/* ---- Chorus ---- */

JNIEXPORT jint JNICALL
Java_dev_kotlinds_fluidsynthkmp_FluidSynthJni_setChorusNr(JNIEnv *env, jobject thiz, jlong synth_ptr, jint nr) {
    if (!synth_ptr) return -1;
    return fluid_synth_set_chorus_group_nr((fluid_synth_t *)(intptr_t)synth_ptr, -1, nr);
}

JNIEXPORT jint JNICALL
Java_dev_kotlinds_fluidsynthkmp_FluidSynthJni_setChorusLevel(JNIEnv *env, jobject thiz, jlong synth_ptr, jdouble level) {
    if (!synth_ptr) return -1;
    return fluid_synth_set_chorus_group_level((fluid_synth_t *)(intptr_t)synth_ptr, -1, level);
}

JNIEXPORT jint JNICALL
Java_dev_kotlinds_fluidsynthkmp_FluidSynthJni_setChorusSpeed(JNIEnv *env, jobject thiz, jlong synth_ptr, jdouble speed) {
    if (!synth_ptr) return -1;
    return fluid_synth_set_chorus_group_speed((fluid_synth_t *)(intptr_t)synth_ptr, -1, speed);
}

JNIEXPORT jint JNICALL
Java_dev_kotlinds_fluidsynthkmp_FluidSynthJni_setChorusDepth(JNIEnv *env, jobject thiz, jlong synth_ptr, jdouble depth) {
    if (!synth_ptr) return -1;
    return fluid_synth_set_chorus_group_depth((fluid_synth_t *)(intptr_t)synth_ptr, -1, depth);
}

/* ---- Render ---- */

JNIEXPORT jfloatArray JNICALL
Java_dev_kotlinds_fluidsynthkmp_FluidSynthJni_renderFloat(JNIEnv *env, jobject thiz, jlong synth_ptr, jint frames) {
    if (!synth_ptr || frames <= 0) return NULL;

    float *left  = (float *)malloc(frames * sizeof(float));
    float *right = (float *)malloc(frames * sizeof(float));
    if (!left || !right) {
        free(left);
        free(right);
        return NULL;
    }

    fluid_synth_write_float(
        (fluid_synth_t *)(intptr_t)synth_ptr,
        frames,
        left,  0, 1,
        right, 0, 1
    );

    jfloatArray result = (*env)->NewFloatArray(env, frames * 2);
    if (result) {
        float *buf = (*env)->GetFloatArrayElements(env, result, NULL);
        for (int i = 0; i < frames; i++) {
            buf[i * 2]     = left[i];
            buf[i * 2 + 1] = right[i];
        }
        (*env)->ReleaseFloatArrayElements(env, result, buf, 0);
    }

    free(left);
    free(right);
    return result;
}

/* ---- Player tick helpers ---- */

JNIEXPORT jint JNICALL
Java_dev_kotlinds_fluidsynthkmp_FluidSynthJni_playerGetCurrentTick(JNIEnv *env, jobject thiz, jlong player_ptr) {
    if (!player_ptr) return 0;
    return fluid_player_get_current_tick((fluid_player_t *)(intptr_t)player_ptr);
}

JNIEXPORT jint JNICALL
Java_dev_kotlinds_fluidsynthkmp_FluidSynthJni_playerGetTotalTicks(JNIEnv *env, jobject thiz, jlong player_ptr) {
    if (!player_ptr) return 0;
    return fluid_player_get_total_ticks((fluid_player_t *)(intptr_t)player_ptr);
}

JNIEXPORT jint JNICALL
Java_dev_kotlinds_fluidsynthkmp_FluidSynthJni_playerSeek(JNIEnv *env, jobject thiz, jlong player_ptr, jint ticks) {
    if (!player_ptr) return -1;
    return fluid_player_seek((fluid_player_t *)(intptr_t)player_ptr, ticks);
}
