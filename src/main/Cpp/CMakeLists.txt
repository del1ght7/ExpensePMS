cmake_minimum_required(VERSION 3.4.1)

add_library( # Указываем имя библиотеки
        native-lib
        SHARED
        native-lib.c)

find_library( # Находим библиотеку log для ведения логов
        log-lib
        log)

target_link_libraries( # Линкуем библиотеку с log-lib
        native-lib
        ${log-lib})