import os
import shutil
import subprocess
import urllib
import urllib.parse
import xml.etree.ElementTree as ET

# maven 仓库地址
repo = 'https://repo1.maven.org/maven2/'
# Bash模板
sh_template = '''
#!/bin/bash

# 检查第一个参数是否是 "-j"
if [ "$1" == "-j" ]; then
    # 如果是，检查是否有第二个参数跟随
    if [ -n "$2" ]; then
        java_path="$2"
        shift 2 # 移除前两个参数，以便后续处理
    else
        echo "错误：'-j' 参数后必须跟一个 Java 路径。"
        exit 1
    fi
else
    # 如果没有 "-j" 参数，默认使用 'which java' 来查找 java 路径
    java_path=$(which java)
fi

# 检查 java 是否可用
if [ -x "$java_path" ]; then
    echo "已找到Java环境: ${java_path}"
else
    echo "未找到Java环境，请安装Java环境后再运行脚本。"
    read -p "按Enter退出..."
    exit 1
fi
mkdir libs
# 创建一个空的关联数组
declare -A files
# 添加元素
{ADD_URLS}

# 定义一个列表
jars={MAIN_JAR}
# 遍历文件列表
for file in "${!files[@]}"; do
    url="${files[$file]}"
    if [ -f "$file" ]; then
        echo "$file 已存在，跳过下载。"
    else
        echo "下载 $file ..."
        # 下载文件
        curl -o "$file" "$url"
    fi
    # 添加到 JAR 列表
    jars+=":$file"
done

echo "开始运行..."
# 运行
java -cp "$jars" {MAIN_CLASS}

read -p "按Enter退出..."
'''


# TODO 添加Bat模板


def get_dependencies_from_pom(pom_file):
    """
    从 pom.xml 文件中读取所有依赖项的 groupId, artifactId, 和 version。

    :param pom_file: pom.xml 文件的路径
    :return: 一个包含所有依赖项信息的列表，每个元素是一个包含 (groupId, artifactId, version) 的元组
    """
    dependencies = []

    # 解析 XML 文件
    tree = ET.parse(pom_file)
    root = tree.getroot()

    # Maven POM XML 的命名空间
    ns = {'m': 'http://maven.apache.org/POM/4.0.0'}

    # 查找所有依赖项
    for dependency in root.findall('m:dependencies/m:dependency', ns):
        group_id = dependency.find('m:groupId', ns)
        artifact_id = dependency.find('m:artifactId', ns)
        version = dependency.find('m:version', ns)

        if group_id is not None and artifact_id is not None and version is not None:
            dependencies.append((
                group_id.text,
                artifact_id.text,
                version.text
            ))

    return dependencies


def get_jar_filename_from_pom(pom_file):
    """
    从 pom.xml 文件中读取构建后的 JAR 文件名。

    :param pom_file: pom.xml 文件的路径
    :return: JAR 文件名字符串
    """
    try:
        # 解析 XML 文件
        tree = ET.parse(pom_file)
        root = tree.getroot()

        # Maven POM XML 的命名空间
        ns = {'m': 'http://maven.apache.org/POM/4.0.0'}

        # 查找 artifactId、version 和 packaging
        artifact_id_elem = root.find('m:artifactId', ns)
        version_elem = root.find('m:version', ns)
        packaging_elem = root.find('m:packaging', ns)

        artifact_id = artifact_id_elem.text if artifact_id_elem is not None else 'unknown'
        version = version_elem.text if version_elem is not None else '0.0.0'
        packaging = packaging_elem.text if packaging_elem is not None else 'jar'

        # 构造文件名
        jar_filename = f"{artifact_id}-{version}.{packaging}"

        return jar_filename
    except ET.ParseError as e:
        print(f"Error parsing XML file: {e}")
        return None
    except Exception as e:
        print(f"An error occurred: {e}")
        return None


def get_maven_url(repo, group_id, artifact_id, version, extension='jar'):
    """
    从 Maven 仓库构造文件的 URL 和文件名。

    :param repo: Maven 仓库的基础 URL
    :param group_id: Maven Group ID
    :param artifact_id: Maven Artifact ID
    :param version: Maven Version
    :param extension: 文件的扩展名 (默认为 'jar')
    :return: 文件名和 URL 的元组
    """
    # 转换 groupId 为路径格式
    group_path = group_id.replace('.', '/')

    # 构造文件名
    file_name = f"{artifact_id}-{version}.{extension}"

    # 构造 URL
    file_url = urllib.parse.urljoin(
        repo,
        f"{group_path}/{artifact_id}/{version}/{file_name}"
    )

    return file_name, file_url


def run_command(command):
    """
    运行系统命令并获取其输出。

    :param command: 要执行的命令字符串
    :return: (stdout, stderr) 元组，其中 stdout 是标准输出，stderr 是标准错误输出
    """
    try:
        # 执行命令
        result = subprocess.run(
            command,
            shell=True,  # 使用 shell 执行命令
            check=True,  # 命令失败时抛出异常
            stdout=subprocess.PIPE,  # 捕获标准输出
            stderr=subprocess.PIPE,  # 捕获标准错误
            text=True  # 以字符串形式返回输出
        )
        return result.stdout, result.stderr
    except subprocess.CalledProcessError as e:
        # 处理错误
        print(f"Command '{e.cmd}' failed with exit code {e.returncode}")
        print(f"Error Output: {e.stderr}")
        return e.stdout, e.stderr


print("start building java")
# 创建output目录
if not os.path.exists('output'):
    os.mkdir('output')
# 运行mvn命令进行编译
print(run_command("mvn clean package")[0])
# 读取jar文件名
jar_filename = get_jar_filename_from_pom('pom.xml')
if jar_filename is not None:
    # 移动jar文件到output目录
    shutil.move(f'target/{jar_filename}', f'output/{jar_filename}')
print("built java completely")

# 构建脚本
print("start building scripts")
# 读取依赖
depends = get_dependencies_from_pom('pom.xml')
# bash的declare命令列表
bash_declare_commands = []
# 遍历依赖
for group_id, artifact_id, version in depends:
    # 获取文件名和 URL
    file_name, file_url = get_maven_url(
        repo,
        group_id,
        artifact_id,
        version
    )
    # 组建为bash命令
    bash_declare_commands.append(f'files["libs/{file_name}"]="{file_url}"')

# 构建bash脚本
bash_script = sh_template.replace("{ADD_URLS}", '\n'.join(bash_declare_commands))
bash_script = bash_script.replace("{MAIN_JAR}", f'{jar_filename}')
bash_script = bash_script.replace("{MAIN_CLASS}", 'org.sparklely.starbot.Main')
# 写入文件
with open('output/run.sh', 'w') as f:
    f.write(bash_script)
    # 添加执行权限
    os.chmod('output/run.sh', 0o755)

print("built scripts completely")
