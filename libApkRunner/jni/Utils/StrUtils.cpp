/*
 * StrUtils.cpp
 *
 *  Created on: 2018年7月12日
 *      Author: yichou
 */
#include <stdint.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <fcntl.h>


uint8_t str_start_with(const char * str, const char * pre)
{
	if(!str || !pre)
		return 0;

    while(*pre)
    {
        if(*pre++ != *str++)
            return 0;
    }

    return 1;
}

uint8_t str_eq_with(const char * a, const char * b)
{
	if(!a || !b)
		return 0;

	while(*a && *b)
	{
		if(*a++ != *b++)
			return 0;
	}

	return *a == *b; //如果相同，最终应该都是 0
}

uint8_t str_end_with(const char * str, const char * end)
{
	if(!str || !end)
		return 0;

	int sl = strlen(str);
	int el = strlen(end);
//	int i;

	if(el > sl)
		return 0;

	return str_eq_with(str + (sl - el), end);
}

uint8_t str_contains(const char * src, const char * dst)
{
	if(!src || !dst)
		return 0;
	if(strstr(src, dst))
		return 1;
	return 0;
}

uint8_t str_is_digit(const char * str)
{
	if(!str || !*str) //空字符串
		return 0;

	while(*str) {
		if(!isdigit(*str++))
			return 0;
	}
	return 1;
}

int str_to_int(const char * str)
{
	if(!str || !*str || !str_is_digit(str)) //空字符串
		return 0;
	return atoi(str);
}

pid_t get_ppid(pid_t pid)
{
	char buffer[128];
	pid_t ppid = 0;

	sprintf(buffer, "/proc/%d/stat", pid);
	FILE* fp = fopen(buffer, "r");
	if (fp) {
		size_t size = fread(buffer, sizeof (char), sizeof (buffer), fp);
		if (size > 0) {
			char * p;

			// See: http://man7.org/linux/man-pages/man5/proc.5.html section /proc/[pid]/stat
			strtok_r(buffer, " ", &p); // (1) pid  %d
			strtok_r(NULL, " ", &p); // (2) comm  %s
			strtok_r(NULL, " ", &p); // (3) state  %c
			char * s_ppid = strtok_r(NULL, " ", &p); // (4) ppid  %d
			ppid = atoi(s_ppid);
		}
		fclose(fp);
	}

	return ppid;
}

/**
 * 获取进程名
 *
 * 往 name 里填，未校验缓冲区长度！
 */
void get_pname(pid_t pid, char * name, size_t bufsize)
{
	char procfile[128];

	sprintf(procfile, "/proc/%d/cmdline", pid);
	FILE* f = fopen(procfile, "r");
	if (f) {
		size_t size;
		size = fread(name, sizeof (char), bufsize, f);
		if (size > 0) {
			if ('\n' == name[size - 1])
				name[size - 1] = '\0';
		}
		fclose(f);
	}
}

int rand_int(int max)
{
	return 1 + (int)((float)max * rand() / (RAND_MAX + 1.0));
}

void str_to_file(char *path, char *str)
{
	FILE* f = fopen(path, "w+"); //创建或清空
	if(f) {
		fputs(str, f);
		fclose(f);
	}
}
