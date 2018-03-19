local result_list = {}
local i = 3

for _, key in pairs(KEYS) do
	local score_before = redis.call('ZSCORE', key, ARGV[i])
	local score_after = 0

	if score_before then
		score_after = math.floor(score_before) + tonumber(ARGV[1]) + tonumber(ARGV[2])
	else
		score_after = tonumber(ARGV[1]) + tonumber(ARGV[2])
	end

	redis.call('ZADD', key, score_after, ARGV[i])
	table.insert(result_list, tostring(score_after))
	i = i + 1
end

return result_list
