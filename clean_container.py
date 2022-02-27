import os

import requests


def read_json(user_name, package_name, access_token):
    url = f"https://api.github.com/users/{user_name}/packages/container/{package_name}/versions"
    headers = {
        "Accept": "application/vnd.github.v3+json",
        "Authorization": f"token {access_token}",
    }
    return requests.get(url, headers=headers).json()


def get_tag_array(container_object):
    return container_object["metadata"]["container"]["tags"]


def get_empty_tag_containers(container_array):
    return [(c["id"], c["name"]) for c in container_array if len(get_tag_array(c)) == 0]


def delete_container(user_name, package_name, access_token, container_id, container_name):
    url = f"https://api.github.com/users/{user_name}/packages/container/{package_name}/versions/{container_id}"
    headers = {
        "Accept": "application/vnd.github.v3+json",
        "Authorization": f"token {access_token}",
    }
    requests.delete(url, headers=headers)
    print(f"Deleted {container_id}({container_name}) in {package_name}")


def main():
    user_name = os.getenv("USER_NAME")
    package_name = os.getenv("PACKAGE_NAME")
    access_token = os.getenv("TOKEN_GITHUB")
    container_array = read_json(user_name, package_name, access_token)
    to_delete = get_empty_tag_containers(container_array)
    for delete_id, delete_name in to_delete:
        delete_container(user_name, package_name, access_token, delete_id, delete_name)


if __name__ == '__main__':
    main()
