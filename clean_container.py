import argparse
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
    parser = argparse.ArgumentParser()
    parser.add_argument("-u", "--username", type=str, help="Username used to login GitHub")
    parser.add_argument("-p", "--package", type=str, help="Package name where images to be removed belong")
    parser.add_argument("--token", type=str, help="GitHub PAT")
    parser.add_argument("--dry", help="Dry run", action="store_true")
    args = parser.parse_args()
    user_name = os.getenv("USER_NAME") or args.username
    package_name = os.getenv("PACKAGE_NAME") or args.package
    access_token = os.getenv("TOKEN_GITHUB") or args.token
    assert user_name and package_name and access_token, "Parameters must be set."
    container_array = read_json(user_name, package_name, access_token)
    to_delete = get_empty_tag_containers(container_array)
    if args.dry:
        print("Found")
        for d in container_array:
            print(d)
        print("To remove")
        for delete_id, delete_name in to_delete:
            print(f"{delete_id}({delete_name}) in {package_name}")
    else:
        for delete_id, delete_name in to_delete:
            delete_container(user_name, package_name, access_token, delete_id, delete_name)


if __name__ == '__main__':
    main()
