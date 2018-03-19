local result_list = {}
local i = 2

local tuning_factor = tonumber(ARGV[1])

for _, key in pairs(KEYS) do
	local score_before = redis.call('ZSCORE', key, ARGV[i])
	local score_to_add = tonumber(ARGV[i + 1])
	local score_after = 0

	if score_before then
		score_after = math.floor(score_before) + score_to_add + tuning_factor
	else
		score_after = score_to_add + tuning_factor
	end

	if score_after < 1 then
		redis.call('ZREM', key, ARGV[i])
	else
		redis.call('ZADD', key, score_after, ARGV[i])
	end

	table.insert(result_list, tostring(score_after))
	i = i + 2
end

return result_list
