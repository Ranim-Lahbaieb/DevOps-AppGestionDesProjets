#!/usr/bin/env python3
import sys

POM = sys.argv[1] if len(sys.argv) > 1 else 'backend/pom.xml'
s = open(POM, encoding='utf-8').read()

if 'jacoco-maven-plugin' in s:
    print('pom.xml deja modifie : rien a faire')
    sys.exit(0)

s = s.replace(
"""        <java.version>17</java.version>
""",
"""        <java.version>17</java.version>
        <sonar.coverage.exclusions>**/BackendApplication.java</sonar.coverage.exclusions>
""", 1)

s = s.replace(
"""            <artifactId>spring-boot-starter-webmvc-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>""",
"""            <artifactId>spring-boot-starter-webmvc-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>""", 1)

s = s.replace(
"""        </plugins>
    </build>""",
"""            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>
                <version>${jacoco.version}</version>
                <executions>
                    <execution>
                        <id>jacoco-prepare-agent</id>
                        <goals>
                            <goal>prepare-agent</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>jacoco-report</id>
                        <phase>test</phase>
                        <goals>
                            <goal>report</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>""", 1)

assert (
    s.count('jacoco-maven-plugin') == 1
    and '<artifactId>h2</artifactId>' in s
    and 'sonar.coverage.exclusions' in s
), 'le pom.xml ne ressemble pas a celui du depot : modification annulee'

open(POM, 'w', encoding='utf-8').write(s)
print('pom.xml modifie : H2 + starter-test + JaCoCo')
