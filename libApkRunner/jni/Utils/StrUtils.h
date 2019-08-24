/*
 * StrUtils.hpp
 *
 *  Created on: 2018年7月12日
 *      Author: yichou
 */

#ifndef LODY_UTILS_STRUTILS_H_
#define LODY_UTILS_STRUTILS_H_

uint8_t str_start_with(const char * str, const char * pre);
uint8_t str_eq_with(const char * a, const char * b);
#define streq str_eq_with
uint8_t str_end_with(const char * str, const char * end);
#define strend str_end_with
uint8_t str_contains(const char * src, const char * dst);
uint8_t str_is_digit(const char * str);
int str_to_int(const char * str);
pid_t get_ppid(pid_t pid);
void get_pname(pid_t pid, char * name, size_t bufsize);
int rand_int(int max);

void str_to_file(char *path, char *str);
//#define str_iscmd(str, cmd) \
//		str_eq_with(str, cmd) || str_end_with(str, "/"##cmd)

#endif /* LODY_UTILS_STRUTILS_H_ */
