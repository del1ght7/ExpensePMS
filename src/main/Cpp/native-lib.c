#include <jni.h>

JNIEXPORT jdouble JNICALL
Java_com_example_expense_ExpenseCalculator_calculateTotal(JNIEnv *env, jobject obj, jdoubleArray expenses) {
    jsize length = (*env)->GetArrayLength(env, expenses);
    jdouble *elements = (*env)->GetDoubleArrayElements(env, expenses, NULL);

    jdouble total = 0;
    for (int i = 0; i < length; ++i) {
        total += elements[i];
    }

    (*env)->ReleaseDoubleArrayElements(env, expenses, elements, 0);
    return total;
}