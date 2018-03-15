---
--- Created by zhengweibao.
---
local lockName = KEYS[1];
local lockContent = ARGV[1];

if redis.call("get", lockName) == lockContent then
	return redis.call("del", lockName)
else
	return 0
end